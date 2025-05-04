package com.example.chat.protocol;

public enum MessageType {
    CHAT_REQUEST,
    CHAT_RESPONSE,
    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    LOGOUT_REQUEST,
    LOGOUT_RESPONSE,
    ROOM_CREATE,
    ROOM_JOIN,
    ROOM_LEAVE,
    ROOM_LIST,
    ROOM_REQUEST,
    ROOM_RESPONSE,
    HEARTBEAT_REQUEST,
    HEARTBEAT_RESPONSE,
    MESSAGE_HISTORY_REQUEST,
    MESSAGE_HISTORY_RESPONSE,
    NOTIFICATION,
    ERROR,
    SYSTEM;
    
    /**
     * 根据序号获取消息类型
     * @param code 消息类型序号
     * @return 消息类型，如果序号无效则返回null
     */
    public static MessageType fromCode(byte code) {
        if (code < 0 || code >= values().length) {
            return null;
        }
        return values()[code];
    }
    
    /**
     * 获取消息类型的序号
     * @return 消息类型序号
     */
    public byte getCode() {
        return (byte) ordinal();
    }
}
