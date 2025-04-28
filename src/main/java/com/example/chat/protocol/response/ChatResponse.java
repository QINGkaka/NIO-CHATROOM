package com.example.chat.protocol.response;

import com.example.chat.model.ChatMessage;
import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatResponse extends ProtocolMessage {
    private boolean success;
    private ChatMessage ProtocolMessage;  // 改为 ProtocolMessage
    private String error;
    
    public ChatResponse() {
        setType(MessageType.CHAT_RESPONSE);
    }
}
