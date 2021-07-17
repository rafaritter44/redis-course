package com.github.rafaritter44.redis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public abstract class RedisTest {

  private static Jedis jedis;

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected Jedis getJedis() {
    return jedis;
  }

  @BeforeAll
  public static void beforeAll() {
    jedis = ConnectionFactory.connect();
  }

  @AfterAll
  public static void afterAll() {
    jedis.close();
  }
}
