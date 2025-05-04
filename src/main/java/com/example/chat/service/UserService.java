package com.example.chat.service;

import com.example.chat.model.User;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据ID获取用户
     */
    User getUserById(String userId);
    
    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);
    
    /**
     * 获取所有在线用户
     */
    List<User> getOnlineUsers();
    
    /**
     * 获取所有用户
     */
    List<User> getAllUsers();
    
    /**
     * 更新用户信息
     */
    void updateUser(User user);
    
    /**
     * 创建新用户
     */
    User createUser(User user);
    
    /**
     * 验证用户token，返回用户ID
     */
    String validateToken(String token);
    
    /**
     * 用户登录
     */
    User login(String username, String password);
}
