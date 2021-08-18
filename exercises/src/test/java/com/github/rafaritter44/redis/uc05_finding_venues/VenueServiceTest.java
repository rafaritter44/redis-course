package com.github.rafaritter44.redis.uc05_finding_venues;

import com.github.rafaritter44.redis.RedisTest;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.GeoUnit;

class VenueServiceTest extends RedisTest {

  private VenueService venueService;

  @BeforeEach
  void setUp() {
    venueService = new VenueService(getJedis());
  }

  @Test
  void shouldFindNearbyVenuesByCoordinate() {
    Iterable<Venue> venues = getVenues();
    venues.forEach(venueService::add);

    double longitude = 139.771977;
    double latitude = 35.668024;
    double radius = 5D;
    GeoUnit unit = GeoUnit.KM;

    Set<VenueAndDistance> nearbyVenues =
        venueService.findNearbyVenues(longitude, latitude, radius, unit);
    logCollection(nearbyVenues);
  }

  @Test
  void shouldFindNearbyVenuesByVenue() {
    Iterable<Venue> venues = getVenues();
    venues.forEach(venueService::add);

    String venue = "Olympic Stadium";
    double radius = 25D;
    GeoUnit unit = GeoUnit.KM;

    Set<VenueAndDistance> nearbyVenues = venueService.findNearbyVenues(venue, radius, unit);
    logCollection(nearbyVenues);
  }

  private void logCollection(Collection<?> collection) {
    collection.stream().map(Object::toString).forEach(logger::debug);
  }

  private Iterable<Venue> getVenues() {
    return List.of(
        Venue.builder().name("Olympic Stadium").longitude(139.76632).latitude(35.666754).build(),
        Venue.builder().name("Nippon Budokan").longitude(139.75).latitude(35.693333).build(),
        Venue.builder().name("Makuhari Messe").longitude(140.034722).latitude(35.648333).build(),
        Venue.builder()
            .name("Saitama Super Arena")
            .longitude(139.630833)
            .latitude(35.894889)
            .build(),
        Venue.builder()
            .name("International Stadium Yokohama")
            .longitude(139.606247)
            .latitude(35.510044)
            .build());
  }
}
