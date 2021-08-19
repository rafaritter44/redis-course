package com.github.rafaritter44.redis.uc06_inventory_with_lua;

import com.github.rafaritter44.redis.KeyNameHelper;
import com.github.rafaritter44.redis.MapHelper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import redis.clients.jedis.Jedis;

class EventService {

  private final Jedis jedis;
  private String scriptSha;

  EventService(Jedis jedis) {
    this.jedis = jedis;
  }

  UUID purchase(String customer, String eventSku) {
    Purchase purchase =
        Purchase.builder()
            .orderId(UUID.randomUUID())
            .customer(customer)
            .eventSku(eventSku)
            .state(Purchase.State.RESERVE)
            .build();
    String purchaseKey = getPurchaseKey(purchase);
    Map<String, String> purchaseHash = MapHelper.toMap(purchase);
    jedis.hmset(purchaseKey, purchaseHash);
    return purchase.getOrderId();
  }

  Purchase.State getPurchaseState(UUID orderId) {
    String purchaseKey = getPurchaseKey(orderId);
    String purchaseState = jedis.hget(purchaseKey, "state");
    return Purchase.State.valueOf(purchaseState);
  }

  boolean updatePurchaseState(UUID orderId, Purchase.State purchaseState) {
    if (scriptSha == null) {
      scriptSha =
          jedis.scriptLoad(
              "local current_state = redis.call('HGET', KEYS[1], 'state')"
                  + " local requested_state = ARGV[1]"
                  + " if ((requested_state == 'AUTHORIZE' and current_state == 'RESERVE') or"
                  + " (requested_state == 'FAIL' and current_state == 'RESERVE') or"
                  + " (requested_state == 'FAIL' and current_state == 'AUTHORIZE') or"
                  + " (requested_state == 'COMPLETE' and current_state == 'AUTHORIZE')) then"
                  + " redis.call('HSET', KEYS[1], 'state', requested_state)"
                  + " return 1"
                  + " else"
                  + " return 0"
                  + " end");
    }
    String purchaseKey = getPurchaseKey(orderId);
    List<String> scriptKeys = List.of(purchaseKey);
    List<String> scriptArgs = List.of(purchaseState.toString());
    boolean successfulUpdate = 1L == (long) jedis.evalsha(scriptSha, scriptKeys, scriptArgs);
    return successfulUpdate;
  }

  private String getPurchaseKey(Purchase purchase) {
    return getPurchaseKey(purchase.getOrderId());
  }

  private String getPurchaseKey(UUID orderId) {
    return KeyNameHelper.getKey("uc06", "purchase", orderId.toString());
  }
}
