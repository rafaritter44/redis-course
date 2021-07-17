package com.github.rafaritter44.redis.uc01_faceted_search;

import com.github.rafaritter44.redis.KeyNameHelper;
import com.google.gson.Gson;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import redis.clients.jedis.Jedis;

public class EventDAO {

  private static final String KEY_PREFIX = "fs";

  private final Jedis jedis;
  private final Gson gson;

  public EventDAO(Jedis jedis, Gson gson) {
    this.jedis = jedis;
    this.gson = gson;
  }

  public void upsert(Event event) {
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

  public Set<Event> search(Iterable<Facet> facets) {
    String[] facetKeys =
        StreamSupport.stream(facets.spliterator(), false)
            .map(this::getFacetKey)
            .toArray(String[]::new);
    String[] eventKeys = jedis.sinter(facetKeys).toArray(String[]::new);
    List<String> jsonEvents = jedis.mget(eventKeys);
    Set<Event> events =
        jsonEvents
            .stream()
            .map(eventJson -> gson.fromJson(eventJson, Event.class))
            .collect(Collectors.toSet());
    return events;
  }

  private Iterable<Facet> getFacets(Event event) {
    return Set.of(
        new Facet("disabled_access", event.isDisabledAccess()),
        new Facet("medal_event", event.isMedalEvent()),
        new Facet("venue", event.getVenue()));
  }

  private String getFacetKey(Facet facet) {
    return KeyNameHelper.getKey(KEY_PREFIX, facet.getAttribute(), facet.getValue().toString());
  }

  private String getEventKey(Event event) {
    return KeyNameHelper.getKey(KEY_PREFIX, "event", event.getSku());
  }
}
