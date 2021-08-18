package com.github.rafaritter44.redis.uc02_inventory_control;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class Purchase {
  UUID orderId;
  String customer;
  int quantity;
  BigDecimal cost;
  String eventSku;
  Instant time;
}
