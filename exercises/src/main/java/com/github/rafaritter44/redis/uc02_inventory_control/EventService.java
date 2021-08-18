package com.github.rafaritter44.redis.uc02_inventory_control;

import com.github.rafaritter44.redis.KeyNameHelper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

class EventService {

  private final Jedis jedis;
  private final Gson gson;

  EventService(Jedis jedis, Gson gson) {
    this.jedis = jedis;
    this.gson = gson;
  }

  void set(Event event) {
    String eventKey = getEventKey(event);
    Map<String, String> eventHash = toMap(event);
    jedis.hmset(eventKey, eventHash);
  }

  Event get(String eventSku) {
    String eventKey = getEventKey(eventSku);
    Map<String, String> eventHash = jedis.hgetAll(eventKey);
    Event event = fromMap(eventHash, Event.class);
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
    Map<String, String> purchaseHash = toMap(purchase);
    transaction.hmset(purchaseKey, purchaseHash);
    transaction.exec();
    return true;
  }

  private Map<String, String> toMap(Object object) {
    Map<String, String> map = new HashMap<>();
    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(object.getClass());
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
    for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
      String propertyName = propertyDescriptor.getName();
      if ("class".equals(propertyName)) {
        continue;
      }
      Method readMethod = propertyDescriptor.getReadMethod();
      String propertyValue;
      try {
        propertyValue = readMethod.invoke(object).toString();
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
      map.put(propertyName, propertyValue);
    }
    return map;
  }

  private <T> T fromMap(Map<String, String> map, Class<T> type) {
    JsonElement json = gson.toJsonTree(map);
    T object = gson.fromJson(json, type);
    return object;
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
