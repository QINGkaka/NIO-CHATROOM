package com.example.chat.protocol.request;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogoutRequest extends ProtocolMessage {
    public LogoutRequest() {
        setType(MessageType.LOGOUT_REQUEST);
    }
}