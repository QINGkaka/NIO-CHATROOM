package com.example.chat.protocol.response;

import com.example.chat.model.User;
import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoginResponse extends ProtocolMessage {
    private boolean success;
    private String token;
    private User user;
    private String ProtocolMessage;
    private String error;
    
    {
        setType(MessageType.LOGIN_RESPONSE);
    }
}
