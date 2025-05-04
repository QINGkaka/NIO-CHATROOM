package com.example.chat.service.impl;

import com.example.chat.model.User;
import com.example.chat.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    
    // 模拟用户数据库
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    // 用户名到用户ID的映射
    private final Map<String, String> usernameToId = new ConcurrentHashMap<>();
    
    // 模拟token到用户ID的映射
    private final Map<String, String> tokenToUserId = new ConcurrentHashMap<>();
    
    @Override
    public User getUserById(String userId) {
        return users.get(userId);
    }
    
    @Override
    public User getUserByUsername(String username) {
        String userId = usernameToId.get(username);
        if (userId != null) {
            return users.get(userId);
        }
        return null;
    }
    
    @Override
    public List<User> getOnlineUsers() {
        return users.values().stream()
                .filter(User::isOnline)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public void updateUser(User user) {
        if (user != null && user.getId() != null) {
            users.put(user.getId(), user);
        }
    }
    
    @Override
    public User createUser(User user) {
        if (user == null) {
            return null;
        }
        
        // 检查用户名是否已存在
        if (getUserByUsername(user.getUsername()) != null) {
            return null; // 用户名已存在
        }
        
        // 生成用户ID
        String userId = UUID.randomUUID().toString();
        user.setId(userId);
        
        // 保存用户
        users.put(userId, user);
        usernameToId.put(user.getUsername(), userId);
        
        return user;
    }
    
    @Override
    public String validateToken(String token) {
        // 简单的token验证，实际应用中应该使用JWT等
        return tokenToUserId.get(token);
    }
    
    @Override
    public User login(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && password.equals(user.getPassword())) {
            // 生成token
            String token = UUID.randomUUID().toString();
            tokenToUserId.put(token, user.getId());
            
            // 更新用户状态
            user.setOnline(true);
            updateUser(user);
            
            return user;
        }
        return null;
    }
}




