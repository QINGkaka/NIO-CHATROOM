package com.example.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.HashSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    private String roomId;
    private String name;  // 注意这里是 name 而不是 roomName
    private String creator;
    @Builder.Default
    private Set<String> members = new HashSet<>();
    private long createTime;  // 改为 long 类型
    private RoomType type;

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
