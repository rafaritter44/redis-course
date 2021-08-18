package com.github.rafaritter44.redis.uc05_finding_venues;

import com.github.rafaritter44.redis.KeyNameHelper;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.GeoRadiusParam;

class VenueService {

  private final Jedis jedis;

  VenueService(Jedis jedis) {
    this.jedis = jedis;
  }

  void add(Venue venue) {
    String venueSetKey = getVenueSetKey();
    String venueName = venue.getName();
    double longitude = venue.getLongitude();
    double latitude = venue.getLatitude();
    jedis.geoadd(venueSetKey, longitude, latitude, venueName);
  }

  Set<VenueAndDistance> findNearbyVenues(
      double longitude, double latitude, double radius, GeoUnit unit) {
    String venueSetKey = getVenueSetKey();
    GeoRadiusParam geoRadiusParam = new GeoRadiusParam().withDist();
    List<GeoRadiusResponse> geoRadiusResponses =
        jedis.georadius(venueSetKey, longitude, latitude, radius, unit, geoRadiusParam);
    Set<VenueAndDistance> venuesAndDistances = toVenuesAndDistances(geoRadiusResponses);
    return venuesAndDistances;
  }

  Set<VenueAndDistance> findNearbyVenues(String venue, double radius, GeoUnit unit) {
    String venueSetKey = getVenueSetKey();
    GeoRadiusParam geoRadiusParam = new GeoRadiusParam().withDist();
    List<GeoRadiusResponse> geoRadiusResponses =
        jedis.georadiusByMember(venueSetKey, venue, radius, unit, geoRadiusParam);
    Set<VenueAndDistance> venuesAndDistances = toVenuesAndDistances(geoRadiusResponses);
    return venuesAndDistances;
  }

  private Set<VenueAndDistance> toVenuesAndDistances(
      Collection<GeoRadiusResponse> geoRadiusResponses) {
    Set<VenueAndDistance> venuesAndDistances =
        geoRadiusResponses
            .stream()
            .map(
                response -> {
                  String venue = response.getMemberByString();
                  double distance = response.getDistance();
                  VenueAndDistance venueAndDistance = new VenueAndDistance(venue, distance);
                  return venueAndDistance;
                })
            .collect(Collectors.toSet());
    return venuesAndDistances;
  }

  private String getVenueSetKey() {
    return KeyNameHelper.getKey("uc05", "geo", "venues");
  }
}
