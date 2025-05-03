package com.example.chat.protocol;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum StatusCode {
    // 成功
    OK(200, "Success"),
    
    // 客户端错误
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    
    // 服务器错误
    INTERNAL_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    
    // 业务错误
    INVALID_MESSAGE(1000, "Invalid Message Format"),
    ROOM_NOT_EXIST(1001, "Chat Room Not Exist"),
    ROOM_FULL(1002, "Chat Room Is Full"),
    USER_NOT_IN_ROOM(1003, "User Not In Room"),
    
    // 系统错误
    SYSTEM_BUSY(2000, "System Busy"),
    RATE_LIMIT(2001, "Rate Limit Exceeded");

    private final int code;
    private final String message;
}

