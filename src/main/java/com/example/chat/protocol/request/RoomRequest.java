package com.example.chat.protocol.request;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomRequest extends ProtocolMessage {
    private String action;
    private String roomId;
    private String roomName;
    
    public RoomRequest() {
        setType(MessageType.ROOM_REQUEST);
    }
}
