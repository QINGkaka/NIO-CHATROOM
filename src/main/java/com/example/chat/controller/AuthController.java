package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.service.JwtService;
import com.example.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            // 检查用户名是否已存在
            if (userService.getUserByUsername(user.getUsername()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Username already exists"));
            }
            
            // 创建用户
            User createdUser = userService.createUser(user);
            String token = jwtService.generateToken(createdUser.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "user", createdUser,
                            "token", token
                    ));
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to register user"));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        // 使用login方法获取用户对象
        User user = userService.login(username, password);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
        
        String token = jwtService.generateToken(user.getUsername());
        
        return ResponseEntity.ok(Map.of(
                "user", user,
                "token", token
        ));
    }
}





