package com.example.chat.protocol.request;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoomRequest extends ProtocolMessage {
    private String roomId;
    private String roomName;  // Added for room creation
    private String action;    // Added to specify the action (CREATE, JOIN, LEAVE)
    private MessageType type;
    
    {
        setType(MessageType.ROOM_CREATE); // Default type, can be changed via builder
    }
}
