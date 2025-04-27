package com.example.chat.protocol.response;

import com.example.chat.model.ChatMessage;
import com.example.chat.protocol.Message;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatResponse extends Message {
    private boolean success;
    private ChatMessage message;  // 改为 message
    private String error;
    
    public ChatResponse() {
        setType(MessageType.CHAT_RESPONSE);
    }
}
