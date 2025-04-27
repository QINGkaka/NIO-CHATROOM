package com.example.chat.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    private String roomId;
    private String roomName;
    private String creatorId;
    private Set<String> members;
    private long createTime;
    
    public void addMember(String userId) {
        if (members == null) {
            members = new HashSet<>();
        }
        members.add(userId);
    }
    
    public void removeMember(String userId) {
        if (members != null) {
            members.remove(userId);
        }
    }
}
