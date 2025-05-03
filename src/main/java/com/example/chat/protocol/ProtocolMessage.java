package com.example.chat.protocol;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ProtocolMessage {
    // 当前实现
    private MessageType type;
    private String requestId;
    private String sender;
    private String content;
    
    // 建议添加
    private int version = 1;        // 协议版本
    private int statusCode;         // 状态码
    private String statusMessage;   // 状态消息
    private long timestamp;         // 时间戳
}
