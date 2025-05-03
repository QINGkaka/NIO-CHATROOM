package com.example.chat.protocol;

import com.example.chat.protocol.request.*;
import com.example.chat.protocol.response.*;
import com.example.chat.protocol.message.NotificationMessage;

public enum MessageType {
    // 系统消息
    HEARTBEAT_REQUEST(0x00, "心跳请求", HeartbeatRequest.class),
    HEARTBEAT_RESPONSE(0x01, "心跳响应", HeartbeatResponse.class),
    ERROR(0x02, "错误消息", ErrorResponse.class),
    
    // 认证消息
    LOGIN_REQUEST(0x10, "登录请求", LoginRequest.class),
    LOGIN_RESPONSE(0x11, "登录响应", LoginResponse.class),
    LOGOUT_REQUEST(0x12, "登出请求", LogoutRequest.class),
    LOGOUT_RESPONSE(0x13, "登出响应", LogoutResponse.class),
    
    // 聊天消息
    CHAT_REQUEST(0x20, "聊天请求", ChatRequest.class),
    CHAT_RESPONSE(0x21, "聊天响应", ChatResponse.class),
    
    // 房间消息
    ROOM_CREATE(0x30, "创建房间", RoomRequest.class),
    ROOM_JOIN(0x31, "加入房间", RoomRequest.class),
    ROOM_LEAVE(0x32, "离开房间", RoomRequest.class),
    ROOM_LIST(0x33, "房间列表", RoomRequest.class),
    ROOM_RESPONSE(0x34, "房间响应", RoomResponse.class),
    
    // 历史消息
    MESSAGE_HISTORY_REQUEST(0x40, "历史消息请求", MessageHistoryRequest.class),
    MESSAGE_HISTORY_RESPONSE(0x41, "历史消息响应", MessageHistoryResponse.class),
    
    // 系统通知
    NOTIFICATION(0x50, "系统通知", NotificationMessage.class);

    private final int code;
    private final String description;
    private final Class<? extends ProtocolMessage> messageClass;
    
    MessageType(int code, String description, Class<? extends ProtocolMessage> messageClass) {
        this.code = code;
        this.description = description;
        this.messageClass = messageClass;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
    
    public Class<? extends ProtocolMessage> getMessageClass() {
        return messageClass;
    }

    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type code: " + code);
    }
}
