package com.example.chat.service;

import com.example.chat.model.User;
import java.util.List;

public interface UserService {
    User login(String username, String password);
    User register(String username, String password, String nickname);
    User getUserById(String userId);
    User getUserByUsername(String username);
    void updateUser(User user);
    void deleteUser(String userId);
    List<User> getAllUsers();
    boolean isOnline(String userId);
    void setUserOnlineStatus(String userId, boolean online);
    boolean authenticate(String userId, String password);
}
