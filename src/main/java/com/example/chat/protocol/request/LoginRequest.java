package com.example.chat.protocol.request;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginRequest extends ProtocolMessage {
    private String username;
    private String password;
    
    public LoginRequest() {
        setType(MessageType.LOGIN_REQUEST);
    }
}
