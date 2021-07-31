package com.github.rafaritter44.redis.uc02_inventory_control;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.rafaritter44.redis.RedisTest;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EventServiceTest extends RedisTest {

  private EventService eventService;

  @BeforeEach
  public void setUp() {
    eventService = new EventService(getJedis(), new Gson());
  }

  @Test
  public void shouldTryToPurchase() {
    Iterable<Event> events = getEvents();
    events.forEach(eventService::set);

    String customer = "bill";
    String eventSku = "123-ABC-723";

    boolean purchased = eventService.purchase(customer, eventSku, 15_000);
    assertTrue(purchased);

    boolean purchasedAgain = eventService.purchase(customer, eventSku, 10_000);
    assertFalse(purchasedAgain);

    Event event = eventService.get(eventSku);
    logger.debug(event.toString());
  }

  private Iterable<Event> getEvents() {
    return List.of(
        Event.builder()
            .sku("123-ABC-723")
            .name("Men's 100m Final")
            .disabledAccess(true)
            .medalEvent(true)
            .venue("Olympic Stadium")
            .category("Track & Field")
            .capacity(60_102)
            .available(20_000)
            .price(new BigDecimal("25.00"))
            .build(),
        Event.builder()
            .sku("737-DEF-911")
            .name("Women's 4x100m Heats")
            .disabledAccess(true)
            .medalEvent(false)
            .venue("Olympic Stadium")
            .category("Track & Field")
            .capacity(60_102)
            .available(10_000)
            .price(new BigDecimal("19.50"))
            .build(),
        Event.builder()
            .sku("320-GHI-921")
            .name("Womens Judo Qualifying")
            .disabledAccess(false)
            .medalEvent(false)
            .venue("Nippon Budokan")
            .category("Martial Arts")
            .capacity(14_471)
            .available(5_000)
            .price(new BigDecimal("15.25"))
            .build());
  }
}
