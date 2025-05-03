package com.example.chat.protocol;

public class MessageValidator {
    public static void validate(ProtocolMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (message.getType() == null) {
            throw new IllegalArgumentException("Message type cannot be null");
        }
        if (message.getRequestId() == null || message.getRequestId().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        if (message.getTimestamp() <= 0) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
        
        // 根据消息类型进行特定验证
        switch (message.getType()) {
            case CHAT_REQUEST:
                validateChatRequest(message);
                break;
            case LOGIN_REQUEST:
                validateLoginRequest(message);
                break;
            case ROOM_CREATE:
            case ROOM_JOIN:
            case ROOM_LEAVE:
            case ROOM_LIST:
                validateRoomRequest(message);
                break;
            default:
                // 其他消息类型只做基础验证
                break;
        }
    }
    
    private static void validateChatRequest(ProtocolMessage message) {
        if (message.getSender() == null || message.getSender().isEmpty()) {
            throw new IllegalArgumentException("Sender cannot be null for chat request");
        }
        if (message.getContent() == null || message.getContent().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null for chat request");
        }
    }
    
    private static void validateLoginRequest(ProtocolMessage message) {
        if (message.getSender() == null || message.getSender().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (message.getContent() == null || message.getContent().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
    }

    private static void validateRoomRequest(ProtocolMessage message) {
        if (message.getContent() == null || message.getContent().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
    }
}



