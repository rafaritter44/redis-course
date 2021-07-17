package com.github.rafaritter44.redis;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyNameHelper {

  private static final String KEY_SEPARATOR = ":";

  public static String getKey(String prefix, String... rest) {
    return prefix + KEY_SEPARATOR + Stream.of(rest).collect(Collectors.joining(KEY_SEPARATOR));
  }
}
