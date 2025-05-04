package com.example.chat.service;

import com.example.chat.model.ChatMessage;
import java.util.List;

public interface MessageService {
    
    /**
     * 保存消息
     */
    void saveMessage(ChatMessage message);
    
    /**
     * 获取用户的消息历史
     */
    List<ChatMessage> getUserMessages(String userId, String otherUserId, int limit);
    
    /**
     * 获取房间的消息历史
     */
    List<ChatMessage> getRoomMessages(String roomId, int limit);
    
    /**
     * 获取两个用户之间的消息
     */
    List<ChatMessage> getMessagesBetweenUsers(String userId1, String userId2);
    
    /**
     * 发送消息
     */
    void sendMessage(ChatMessage message);
}




