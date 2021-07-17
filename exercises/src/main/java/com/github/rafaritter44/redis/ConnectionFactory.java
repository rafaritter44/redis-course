package com.github.rafaritter44.redis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class ConnectionFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionFactory.class);

  public static Jedis connect() {
    Properties properties = new Properties();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream inputStream = classLoader.getResourceAsStream("redis.properties")) {
      properties.load(inputStream);
    } catch (IOException e) {
      LOGGER.error("Error loading Redis properties", e);
    }

    String host = properties.getProperty("redis.host", "localhost");
    String port = properties.getProperty("redis.port", "6379");
    String database = properties.getProperty("redis.database", "0");
    Optional<String> optionalPassword =
        Optional.ofNullable(properties.getProperty("redis.password"));

    String redisUriString = String.format("redis://%s:%s/%s", host, port, database);
    URI redisUri = URI.create(redisUriString);
    LOGGER.info("Connecting to {}", redisUriString);
    Jedis jedis = new Jedis(redisUri);

    optionalPassword.ifPresent(
        password -> {
          String authResult = jedis.auth(password);
          LOGGER.info("Authentication result: {}", authResult);
        });

    return jedis;
  }
}
