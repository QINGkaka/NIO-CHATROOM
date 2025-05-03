package com.example.chat.protocol.request;

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
public class ChatRequest extends ProtocolMessage {
    private String roomId;
    private String content;
    
    {
        setType(MessageType.CHAT_REQUEST);
    }
}
