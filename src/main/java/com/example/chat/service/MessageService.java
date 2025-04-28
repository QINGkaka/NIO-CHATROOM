package com.example.chat.service;

import com.example.chat.model.ChatMessage;
import java.util.List;

public interface MessageService {
    void sendMessage(ChatMessage ProtocolMessage);
    List<ChatMessage> getRoomMessages(String roomId, int limit, long beforeTime);
    void deleteRoomMessages(String roomId);
    void broadcastMessage(ChatMessage ProtocolMessage);
    
    // 新添加的方法
    long getMessageCount(String roomId);
    List<ChatMessage> searchMessages(String roomId, String keyword, int limit);
    void deleteMessage(String messageId);
    void updateMessage(ChatMessage ProtocolMessage);
}

