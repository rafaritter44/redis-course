package com.github.rafaritter44.redis.uc01_faceted_search;

import lombok.Value;

@Value
class Facet {
  String attribute;
  Object value;
}
