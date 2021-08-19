package com.github.rafaritter44.redis;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MapHelper {

  private static final Gson GSON = new Gson();

  public static Map<String, String> toMap(Object object) {
    Map<String, String> map = new HashMap<>();
    BeanInfo beanInfo;
    try {
      beanInfo = Introspector.getBeanInfo(object.getClass());
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
    for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
      String propertyName = propertyDescriptor.getName();
      if ("class".equals(propertyName)) {
        continue;
      }
      Method readMethod = propertyDescriptor.getReadMethod();
      String propertyValue;
      try {
        propertyValue = readMethod.invoke(object).toString();
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
      map.put(propertyName, propertyValue);
    }
    return map;
  }

  public static <T> T fromMap(Map<String, String> map, Class<T> type) {
    JsonElement json = GSON.toJsonTree(map);
    T object = GSON.fromJson(json, type);
    return object;
  }
}
