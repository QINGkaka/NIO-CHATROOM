package com.example.chat.service.impl;

import com.example.chat.dao.UserDao;
import com.example.chat.model.User;
import com.example.chat.service.UserService;
import com.example.chat.util.PasswordUtil;

import java.util.List;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && PasswordUtil.verify(password, user.getPassword())) {
            user.setOnline(true);
            userDao.update(user);
            return user;
        }
        return null;
    }

    @Override
    public User register(String username, String password, String nickname) {
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
            .userId(UUID.randomUUID().toString())
            .username(username)
            .password(PasswordUtil.hash(password))
            .nickname(nickname)
            .online(false)
            .build();

        userDao.save(user);
        return user;
    }

    @Override
    public User getUserById(String userId) {
        return userDao.findById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public void updateUser(User user) {
        userDao.update(user);
    }

    @Override
    public void deleteUser(String userId) {
        userDao.delete(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public boolean isOnline(String userId) {
        User user = getUserById(userId);
        return user != null && user.isOnline();
    }

    @Override
    public void setUserOnlineStatus(String userId, boolean online) {
        User user = getUserById(userId);
        if (user != null) {
            user.setOnline(online);
            userDao.update(user);
        }
    }

    @Override
    public boolean authenticate(String userId, String password) {
          User user = userDao.findById(userId);
       if (user == null) {
          return false;
      }
        return PasswordUtil.verify(password, user.getPassword());
    }
}
