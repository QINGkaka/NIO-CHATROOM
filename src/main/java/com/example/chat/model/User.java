package com.example.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String username;
    private String password;
    private String profilePicture;
    private String email;
    private boolean online;
    
    // 添加这些方法以兼容现有代码
    public String getFullName() {
        return username; // 简化实现，实际应用中可能需要firstName + lastName
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
