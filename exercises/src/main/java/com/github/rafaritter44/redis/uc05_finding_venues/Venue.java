package com.github.rafaritter44.redis.uc05_finding_venues;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class Venue {
  String name;
  double longitude;
  double latitude;
}
