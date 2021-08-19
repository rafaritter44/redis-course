package com.github.rafaritter44.redis.uc06_inventory_with_lua;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.rafaritter44.redis.RedisTest;
import com.github.rafaritter44.redis.uc06_inventory_with_lua.Purchase.State;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventServiceTest extends RedisTest {

  private EventService eventService;

  @BeforeEach
  void setUp() {
    eventService = new EventService(getJedis());
  }

  @Test
  void shouldUpdatePurchaseState() {
    String customer = "bill";
    String eventSku = "123-ABC-723";

    UUID orderId = eventService.purchase(customer, eventSku);

    State purchaseState = eventService.getPurchaseState(orderId);
    assertEquals(Purchase.State.RESERVE, purchaseState);

    boolean successfulUpdate = eventService.updatePurchaseState(orderId, Purchase.State.COMPLETE);
    purchaseState = eventService.getPurchaseState(orderId);
    assertFalse(successfulUpdate);
    assertEquals(Purchase.State.RESERVE, purchaseState);

    successfulUpdate = eventService.updatePurchaseState(orderId, Purchase.State.AUTHORIZE);
    purchaseState = eventService.getPurchaseState(orderId);
    assertTrue(successfulUpdate);
    assertEquals(Purchase.State.AUTHORIZE, purchaseState);

    successfulUpdate = eventService.updatePurchaseState(orderId, Purchase.State.COMPLETE);
    purchaseState = eventService.getPurchaseState(orderId);
    assertTrue(successfulUpdate);
    assertEquals(Purchase.State.COMPLETE, purchaseState);
  }
}
