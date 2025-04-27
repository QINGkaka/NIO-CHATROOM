package com.example.chat.protocol.request;

import com.example.chat.protocol.Message;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatRequest extends Message {
    private String roomId;
    private String content;
    
    public ChatRequest() {
        setType(MessageType.CHAT_REQUEST);
    }
}
