package com.github.rafaritter44.redis.uc01_faceted_search;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class Event {
  String sku;
  String name;
  boolean disabledAccess;
  boolean medalEvent;
  String venue;
  String category;
}
