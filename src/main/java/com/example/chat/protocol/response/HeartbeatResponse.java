package com.example.chat.protocol.response;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HeartbeatResponse extends ProtocolMessage {
    private long timestamp;
    
    public HeartbeatResponse() {
        setType(MessageType.HEARTBEAT_RESPONSE);
        this.timestamp = System.currentTimeMillis();
    }
}