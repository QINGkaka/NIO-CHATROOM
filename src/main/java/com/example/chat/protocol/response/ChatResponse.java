package com.example.chat.protocol.response;

import com.example.chat.model.ChatMessage;
import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatResponse extends ProtocolMessage {
    private boolean success;
    private ChatMessage message;  // 修正字段名
    private String error;
    
    {
        setType(MessageType.CHAT_RESPONSE);
    }
}
