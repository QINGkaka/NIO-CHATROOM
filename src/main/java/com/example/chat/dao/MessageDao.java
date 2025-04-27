package com.example.chat.dao;

import com.example.chat.model.ChatMessage;
import java.util.List;

public interface MessageDao {
    void save(ChatMessage message);
    List<ChatMessage> findByRoomId(String roomId, int limit, long beforeTime);
    void deleteByRoomId(String roomId);
    long getMessageCount(String roomId);
    List<ChatMessage> searchMessages(String roomId, String keyword, int limit);
    void deleteMessage(String messageId);
    void updateMessage(ChatMessage message);
}
