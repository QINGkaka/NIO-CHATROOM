package com.example.chat.util;

import com.example.chat.model.ChatMessage;
import java.util.UUID;

public class SystemMessageUtil {
    public static ChatMessage createSystemMessage(String roomId, String content) {
        return ChatMessage.builder()
            .messageId(UUID.randomUUID().toString())
            .roomId(roomId)
            .userId("SYSTEM")
            .username("System")
            .content(content)
            .timestamp(System.currentTimeMillis())
            .type(ChatMessage.MessageType.SYSTEM)
            .build();
    }

    public static ChatMessage createUserJoinMessage(String roomId, String username) {
        return createSystemMessage(roomId, username + " joined the room");
    }

    public static ChatMessage createUserLeaveMessage(String roomId, String username) {
        return createSystemMessage(roomId, username + " left the room");
    }
}