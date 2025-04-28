package com.example.chat.protocol.request;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HeartbeatRequest extends ProtocolMessage {
    private String userId;
    
    public HeartbeatRequest() {
        setType(MessageType.HEARTBEAT_REQUEST);
    }
}
