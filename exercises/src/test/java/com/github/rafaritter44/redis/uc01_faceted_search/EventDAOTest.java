package com.github.rafaritter44.redis.uc01_faceted_search;

import com.github.rafaritter44.redis.RedisTest;
import com.google.gson.Gson;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventDAOTest extends RedisTest {

  private EventDAO eventDAO;

  @BeforeEach
  void setUp() {
    eventDAO = new EventDAO(getJedis(), new Gson());
  }

  @Test
  void shouldDoFacetedSearch() {
    Iterable<Event> events = getEvents();
    events.forEach(eventDAO::index);

    Set<Event> searchResult = eventDAO.search(List.of(new Facet("disabled_access", true)));
    logger.debug("disabled_access=true:");
    logCollection(searchResult);

    searchResult =
        eventDAO.search(
            List.of(new Facet("disabled_access", true), new Facet("medal_event", false)));
    logger.debug("disabled_access=true, medal_event=false:");
    logCollection(searchResult);

    searchResult =
        eventDAO.search(
            List.of(
                new Facet("disabled_access", false),
                new Facet("medal_event", false),
                new Facet("venue", "Nippon Budokan")));
    logger.debug("disabled_access=false, medal_event=false, venue=Nippon Budokan:");
    logCollection(searchResult);
  }

  @Test
  void shouldDoHashedFacetedSearch() {
    Iterable<Event> events = getEvents();
    events.forEach(eventDAO::indexHashing);

    Set<Event> searchResult = eventDAO.searchHashed(List.of(new Facet("disabled_access", true)));
    logger.debug("disabled_access=true:");
    logCollection(searchResult);

    searchResult =
        eventDAO.searchHashed(
            List.of(new Facet("disabled_access", true), new Facet("medal_event", false)));
    logger.debug("disabled_access=true, medal_event=false:");
    logCollection(searchResult);

    searchResult =
        eventDAO.searchHashed(
            List.of(
                new Facet("disabled_access", false),
                new Facet("medal_event", false),
                new Facet("venue", "Nippon Budokan")));
    logger.debug("disabled_access=false, medal_event=false, venue=Nippon Budokan:");
    logCollection(searchResult);
  }

  private void logCollection(Collection<?> collection) {
    collection.stream().map(Object::toString).forEach(logger::debug);
  }

  private Iterable<Event> getEvents() {
    return List.of(
        Event.builder()
            .sku("123-ABC-723")
            .name("Men's 100m Final")
            .disabledAccess(true)
            .medalEvent(true)
            .venue("Olympic Stadium")
            .category("Track & Field")
            .build(),
        Event.builder()
            .sku("737-DEF-911")
            .name("Women's 4x100m Heats")
            .disabledAccess(true)
            .medalEvent(false)
            .venue("Olympic Stadium")
            .category("Track & Field")
            .build(),
        Event.builder()
            .sku("320-GHI-921")
            .name("Womens Judo Qualifying")
            .disabledAccess(false)
            .medalEvent(false)
            .venue("Nippon Budokan")
            .category("Martial Arts")
            .build());
  }
}
