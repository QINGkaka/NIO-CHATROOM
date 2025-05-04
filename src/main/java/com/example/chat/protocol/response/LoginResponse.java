package com.example.chat.protocol.response;

import com.example.chat.model.User;
import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginResponse extends AbstractProtocolMessage {
    private boolean success;
    private User user;
    private String error;
    
    public LoginResponse() {
        super(MessageType.LOGIN_RESPONSE);
    }
    
    @Builder
    public LoginResponse(boolean success, User user, String error, String requestId) {
        super(MessageType.LOGIN_RESPONSE);
        this.success = success;
        this.user = user;
        this.error = error;
        this.setRequestId(requestId);
    }
}
