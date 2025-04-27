package com.example.chat.protocol.response;

import com.example.chat.model.User;
import com.example.chat.protocol.Message;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoginResponse extends Message {
    private boolean success;
    private String token;
    private User user;
    private String message;
    private String error;
    
    {
        setType(MessageType.LOGIN_RESPONSE);
    }
}
