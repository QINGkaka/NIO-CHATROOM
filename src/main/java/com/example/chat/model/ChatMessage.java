package com.example.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    private String id;
    private String senderId;
    private String receiverId;
    private String roomId;
    private String content;
    private long timestamp;
    private MessageType type;
    
    public enum MessageType {
        TEXT,
        IMAGE,
        SYSTEM
    }
}
