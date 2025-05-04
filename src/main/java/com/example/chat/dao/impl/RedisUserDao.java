package com.example.chat.dao.impl;

import com.example.chat.dao.UserDao;
import com.example.chat.model.User;
import com.example.chat.util.JsonUtil;
import com.example.chat.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class RedisUserDao implements UserDao {
    
    private static final String USER_KEY_PREFIX = "chat:user:";
    private static final String USER_EMAIL_KEY_PREFIX = "chat:user:email:";
    private static final String USER_USERNAME_KEY_PREFIX = "chat:user:username:";
    private static final String ALL_USERS_KEY = "chat:users:all";
    
    @Override
    public User save(User user) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            // 保存用户信息
            String key = USER_KEY_PREFIX + user.getId();
            jedis.set(key, JsonUtil.toJson(user));
            
            // 保存邮箱索引
            if (user.getEmail() != null) {
                String emailKey = USER_EMAIL_KEY_PREFIX + user.getEmail();
                jedis.set(emailKey, user.getId());
            }
            
            // 保存用户名索引
            if (user.getUsername() != null) {
                String usernameKey = USER_USERNAME_KEY_PREFIX + user.getUsername();
                jedis.set(usernameKey, user.getId());
            }
            
            // 添加到所有用户集合
            jedis.sadd(ALL_USERS_KEY, user.getId());
        }
        return user;
    }
    
    @Override
    public User findById(String userId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String key = USER_KEY_PREFIX + userId;
            String json = jedis.get(key);
            return json != null ? JsonUtil.fromJson(json, User.class) : null;
        }
    }
    
    @Override
    public User findByEmail(String email) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String emailKey = USER_EMAIL_KEY_PREFIX + email;
            String userId = jedis.get(emailKey);
            return userId != null ? findById(userId) : null;
        }
    }
    
    @Override
    public User findByUsername(String username) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String usernameKey = USER_USERNAME_KEY_PREFIX + username;
            String userId = jedis.get(usernameKey);
            return userId != null ? findById(userId) : null;
        }
    }
    
    @Override
    public User update(User user) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String key = USER_KEY_PREFIX + user.getId();
            jedis.set(key, JsonUtil.toJson(user));
        }
        return user; // 返回更新后的用户对象
    }
    
    @Override
    public void delete(String userId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            // 获取用户信息
            User user = findById(userId);
            if (user == null) {
                return;
            }
            
            // 删除用户信息
            String key = USER_KEY_PREFIX + userId;
            jedis.del(key);
            
            // 删除邮箱索引
            if (user.getEmail() != null) {
                String emailKey = USER_EMAIL_KEY_PREFIX + user.getEmail();
                jedis.del(emailKey);
            }
            
            // 删除用户名索引
            if (user.getUsername() != null) {
                String usernameKey = USER_USERNAME_KEY_PREFIX + user.getUsername();
                jedis.del(usernameKey);
            }
            
            // 从所有用户集合中移除
            jedis.srem(ALL_USERS_KEY, userId);
        }
    }
    
    @Override
    public List<User> findAll() {
        try (Jedis jedis = RedisUtil.getJedis()) {
            Set<String> userIds = jedis.smembers(ALL_USERS_KEY);
            List<User> users = new ArrayList<>();
            
            for (String userId : userIds) {
                User user = findById(userId);
                if (user != null) {
                    users.add(user);
                }
            }
            
            return users;
        }
    }
}
