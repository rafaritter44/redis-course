package com.github.rafaritter44.redis.uc01_faceted_search;

import com.github.rafaritter44.redis.KeyNameHelper;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import redis.clients.jedis.Jedis;

class EventDAO {

  private final Jedis jedis;
  private final Gson gson;

  EventDAO(Jedis jedis, Gson gson) {
    this.jedis = jedis;
    this.gson = gson;
  }

  void index(Event event) {
    String eventKey = getEventKey(event);
    String eventJson = gson.toJson(event);
    jedis.set(eventKey, eventJson);

    Iterable<Facet> facets = getFacets(event);
    facets.forEach(
        facet -> {
          String facetKey = getFacetKey(facet);
          jedis.sadd(facetKey, eventKey);
        });
  }

  Set<Event> search(Iterable<Facet> facets) {
    String[] facetKeys =
        StreamSupport.stream(facets.spliterator(), false)
            .map(this::getFacetKey)
            .toArray(String[]::new);
    Set<String> eventKeys = jedis.sinter(facetKeys);
    Set<Event> events = getEvents(eventKeys);
    return events;
  }

  void indexHashing(Event event) {
    String eventKey = getEventKey(event);
    String eventJson = gson.toJson(event);
    jedis.set(eventKey, eventJson);

    Iterable<Facet> facets = getFacets(event);
    List<Facet> seenFacets = new ArrayList<>();
    facets.forEach(
        facet -> {
          seenFacets.add(facet);
          String hashedFacetKey = getHashedFacetKey(seenFacets);
          jedis.sadd(hashedFacetKey, eventKey);
        });
  }

  Set<Event> searchHashed(Iterable<Facet> facets) {
    String hashedFacetKey = getHashedFacetKey(facets);
    Set<String> eventKeys = jedis.smembers(hashedFacetKey);
    Set<Event> events = getEvents(eventKeys);
    return events;
  }

  private Set<Event> getEvents(Collection<String> eventKeys) {
    List<String> jsonEvents = jedis.mget(eventKeys.toArray(String[]::new));
    Set<Event> events =
        jsonEvents
            .stream()
            .map(eventJson -> gson.fromJson(eventJson, Event.class))
            .collect(Collectors.toSet());
    return events;
  }

  private Iterable<Facet> getFacets(Event event) {
    return List.of(
        new Facet("disabled_access", event.isDisabledAccess()),
        new Facet("medal_event", event.isMedalEvent()),
        new Facet("venue", event.getVenue()));
  }

  private String getFacetKey(Facet facet) {
    return KeyNameHelper.getKey("uc01", "fs", facet.getAttribute(), facet.getValue().toString());
  }

  private String getHashedFacetKey(Iterable<Facet> facets) {
    String hashedFacets = hash(facets);
    return KeyNameHelper.getKey("uc01", "hfs", hashedFacets);
  }

  private String hash(Object object) {
    return Hashing.sha256().hashString(object.toString(), StandardCharsets.UTF_8).toString();
  }

  private String getEventKey(Event event) {
    return KeyNameHelper.getKey("uc01", "event", event.getSku());
  }
}
