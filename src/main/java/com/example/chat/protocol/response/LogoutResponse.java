package com.example.chat.protocol.response;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogoutResponse extends ProtocolMessage {
    private boolean success;
    private String message;
    
    public LogoutResponse() {
        setType(MessageType.LOGOUT_RESPONSE);
    }
}