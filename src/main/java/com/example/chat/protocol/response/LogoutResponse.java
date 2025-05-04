package com.example.chat.protocol.response;

import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogoutResponse extends AbstractProtocolMessage {
    private boolean success;
    private String message;
    
    public LogoutResponse() {
        super(MessageType.LOGOUT_RESPONSE);
    }
    
    @Builder
    public LogoutResponse(boolean success, String message, String requestId) {
        super(MessageType.LOGOUT_RESPONSE);
        this.success = success;
        this.message = message;
        this.setRequestId(requestId);
    }
}



