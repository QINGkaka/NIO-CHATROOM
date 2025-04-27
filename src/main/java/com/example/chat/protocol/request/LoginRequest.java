package com.example.chat.protocol.request;

import com.example.chat.protocol.Message;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginRequest extends Message {
    private String username;
    private String password;
    
    public LoginRequest() {
        setType(MessageType.LOGIN_REQUEST);
    }
}
