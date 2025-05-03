package com.example.chat.protocol.response;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.StatusCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends ProtocolMessage {
    private StatusCode status;  // 使用 StatusCode 枚举
    private String details;

    public ErrorResponse(StatusCode status) {
        this.status = status;
        this.setStatusCode(status.getCode());
        this.setStatusMessage(status.getMessage());
    }

    public ErrorResponse(StatusCode status, String details) {
        this(status);
        this.details = details;
    }
}
