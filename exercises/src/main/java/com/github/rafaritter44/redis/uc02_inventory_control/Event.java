package com.github.rafaritter44.redis.uc02_inventory_control;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Event {
  String sku;
  String name;
  boolean disabledAccess;
  boolean medalEvent;
  String venue;
  String category;
  int capacity;
  int available;
  BigDecimal price;
}
