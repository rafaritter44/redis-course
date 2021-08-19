package com.github.rafaritter44.redis.uc06_inventory_with_lua;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Purchase {

  public enum State {
    RESERVE,
    AUTHORIZE,
    FAIL,
    COMPLETE;
  }

  UUID orderId;
  String customer;
  String eventSku;
  State state;
}
