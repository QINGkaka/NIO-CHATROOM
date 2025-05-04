package com.example.chat.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolMessage {
    private MessageType type;
    private int statusCode;
    private String requestId;
    private long timestamp;
    private String sender;
    private String content;
    private String roomId;
    
    // 构造函数
    public ProtocolMessage(MessageType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.requestId = java.util.UUID.randomUUID().toString();
    }
}





