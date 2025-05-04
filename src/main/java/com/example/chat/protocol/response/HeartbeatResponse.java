package com.example.chat.protocol.response;

import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HeartbeatResponse extends AbstractProtocolMessage {
    private long serverTime;
    
    public HeartbeatResponse() {
        super(MessageType.HEARTBEAT_RESPONSE);
        this.serverTime = System.currentTimeMillis();
    }
    
    @Builder
    public HeartbeatResponse(String requestId) {
        super(MessageType.HEARTBEAT_RESPONSE);
        this.serverTime = System.currentTimeMillis();
        this.setRequestId(requestId);
    }
}





