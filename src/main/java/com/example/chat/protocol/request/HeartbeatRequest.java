package com.example.chat.protocol.request;

import com.example.chat.protocol.Message;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HeartbeatRequest extends Message {
    private String userId;
    
    public HeartbeatRequest() {
        setType(MessageType.HEARTBEAT_REQUEST);
    }
}
