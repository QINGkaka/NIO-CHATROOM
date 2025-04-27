package com.example.chat.protocol;

public enum MessageType {
    // 请求消息类型
    LOGIN,
    LOGIN_REQUEST,
    CHAT_REQUEST,
    ROOM_REQUEST,
    HEARTBEAT_REQUEST,
    MESSAGE_HISTORY_REQUEST,
    
    // 响应消息类型
    ERROR,
    LOGIN_RESPONSE,
    CHAT_RESPONSE,
    ROOM_RESPONSE,
    HEARTBEAT_RESPONSE,
    MESSAGE_HISTORY_RESPONSE
}
