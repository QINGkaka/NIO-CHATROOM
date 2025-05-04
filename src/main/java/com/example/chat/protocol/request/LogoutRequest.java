package com.example.chat.protocol.request;

import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogoutRequest extends AbstractProtocolMessage {
    
    public LogoutRequest() {
        super(MessageType.LOGOUT_REQUEST);
    }
    
    @Builder
    public LogoutRequest(String userId, String requestId) {
        super(MessageType.LOGOUT_REQUEST);
        this.setSender(userId);
        this.setRequestId(requestId);
    }
}



