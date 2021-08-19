package com.github.rafaritter44.redis.uc02_inventory_control;

import com.github.rafaritter44.redis.KeyNameHelper;
import com.github.rafaritter44.redis.MapHelper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

class EventService {

  private final Jedis jedis;

  EventService(Jedis jedis) {
    this.jedis = jedis;
  }

  void set(Event event) {
    String eventKey = getEventKey(event);
    Map<String, String> eventHash = MapHelper.toMap(event);
    jedis.hmset(eventKey, eventHash);
  }

  Event get(String eventSku) {
    String eventKey = getEventKey(eventSku);
    Map<String, String> eventHash = jedis.hgetAll(eventKey);
    Event event = MapHelper.fromMap(eventHash, Event.class);
    return event;
  }

  boolean purchase(String customer, String eventSku, int quantity) {
    String eventKey = getEventKey(eventSku);
    jedis.watch(eventKey);
    int available = Integer.valueOf(jedis.hget(eventKey, "available"));
    if (quantity > available) {
      jedis.unwatch();
      return false;
    }
    BigDecimal price = new BigDecimal(jedis.hget(eventKey, "price"));
    BigDecimal cost = price.multiply(new BigDecimal(quantity));
    Purchase purchase =
        Purchase.builder()
            .orderId(UUID.randomUUID())
            .customer(customer)
            .quantity(quantity)
            .cost(cost)
            .eventSku(eventSku)
            .time(Instant.now())
            .build();
    Transaction transaction = jedis.multi();
    transaction.hincrBy(eventKey, "available", -quantity);
    String purchaseKey = getPurchaseKey(purchase);
    Map<String, String> purchaseHash = MapHelper.toMap(purchase);
    transaction.hmset(purchaseKey, purchaseHash);
    transaction.exec();
    return true;
  }

  private String getEventKey(Event event) {
    return getEventKey(event.getSku());
  }

  private String getEventKey(String eventSku) {
    return KeyNameHelper.getKey("uc02", "event", eventSku);
  }

  private String getPurchaseKey(Purchase purchase) {
    return KeyNameHelper.getKey("uc02", "purchase", purchase.getOrderId().toString());
  }
}
