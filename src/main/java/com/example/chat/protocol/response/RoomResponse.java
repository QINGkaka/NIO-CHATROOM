package com.example.chat.protocol.response;

import com.example.chat.model.Room;
import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomResponse extends AbstractProtocolMessage {
    private Room room;
    private boolean success;
    
    public RoomResponse() {
        super(MessageType.ROOM_RESPONSE);
    }
    
    @Builder
    public RoomResponse(Room room, boolean success, String requestId) {
        super(MessageType.ROOM_RESPONSE);
        this.room = room;
        this.success = success;
        this.setRequestId(requestId);
    }
}
