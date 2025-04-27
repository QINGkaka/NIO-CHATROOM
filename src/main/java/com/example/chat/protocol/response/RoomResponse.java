package com.example.chat.protocol.response;

import com.example.chat.model.ChatRoom;
import com.example.chat.protocol.Message;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomResponse extends Message {
    private boolean success;
    private ChatRoom room;
    private String error;
    
    public RoomResponse() {
        setType(MessageType.ROOM_RESPONSE);
    }
}
