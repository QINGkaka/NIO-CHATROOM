package com.example.chat.protocol.response;

import com.example.chat.protocol.ProtocolMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends ProtocolMessage {
    private int code;
    private String error;
    private String details;
}
