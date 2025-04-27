package com.example.chat.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisUtil {
    private static JedisPool jedisPool;
    
    public static void init(String host, int port, String password) {
        try {
            jedisPool = new JedisPool(host, port);
        } catch (Exception e) {
            log.error("Redis init failed", e);
        }
    }
    
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
    
    public static void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
    
    public static void shutdown() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
