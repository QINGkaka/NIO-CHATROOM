package com.example.chat.dao;

import com.example.chat.model.User;
import java.util.List;

public interface UserDao {
    User findById(String userId);
    User findByUsername(String username);
    void save(User user);
    void update(User user);
    void delete(String userId);
    List<User> findAll();
    boolean exists(String username);
}
