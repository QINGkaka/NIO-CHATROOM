package com.example.chat.dao.impl;

import com.example.chat.dao.UserDao;
import com.example.chat.model.User;
import com.example.chat.util.JsonUtil;
import com.example.chat.util.RedisUtil;
import redis.clients.jedis.Jedis;
import java.util.List;
import java.util.stream.Collectors;

public class RedisUserDao implements UserDao {
    private static final String USER_KEY_PREFIX = "user:";
    private static final String USERNAME_INDEX_KEY = "username:index";

    @Override
    public User findById(String userId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String json = jedis.get(USER_KEY_PREFIX + userId);
            return json != null ? JsonUtil.fromJson(json, User.class) : null;
        }
    }

    @Override
    public User findByUsername(String username) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String userId = jedis.hget(USERNAME_INDEX_KEY, username);
            return userId != null ? findById(userId) : null;
        }
    }

    @Override
    public void save(User user) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String json = JsonUtil.toJson(user);
            jedis.set(USER_KEY_PREFIX + user.getUserId(), json);
            jedis.hset(USERNAME_INDEX_KEY, user.getUsername(), user.getUserId());
        }
    }

    @Override
    public void update(User user) {
        save(user);
    }

    @Override
    public void delete(String userId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            User user = findById(userId);
            if (user != null) {
                jedis.del(USER_KEY_PREFIX + userId);
                jedis.hdel(USERNAME_INDEX_KEY, user.getUsername());
            }
        }
    }

    @Override
    public List<User> findAll() {
        try (Jedis jedis = RedisUtil.getJedis()) {
            return jedis.hvals(USERNAME_INDEX_KEY).stream()
                .map(this::findById)
                .collect(Collectors.toList());
        }
    }

    @Override
    public boolean exists(String username) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            return jedis.hexists(USERNAME_INDEX_KEY, username);
        }
    }
}
