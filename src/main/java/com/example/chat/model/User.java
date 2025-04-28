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
    private String userId;
    private String username;
    private String password;
    private String nickname;
    private UserStatus status;
    private boolean online;

    public void setOnline(boolean online) {
        this.online = online;
        this.status = online ? UserStatus.ONLINE : UserStatus.OFFLINE;
    }

    public boolean isOnline() {
        return this.online;
    }
}
