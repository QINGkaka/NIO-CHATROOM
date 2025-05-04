package com.example.chat.protocol.request;

import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginRequest extends AbstractProtocolMessage {
    private String username;
    private String password;
    
    public LoginRequest() {
        super(MessageType.LOGIN_REQUEST);
    }
    
    @Builder
    public LoginRequest(String username, String password, String requestId) {
        super(MessageType.LOGIN_REQUEST);
        this.username = username;
        this.password = password;
        this.setSender(username);
        this.setRequestId(requestId);
    }
}
