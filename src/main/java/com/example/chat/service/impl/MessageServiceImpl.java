package com.example.chat.service.impl;

import com.example.chat.model.ChatMessage;
import com.example.chat.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {
    
    // 模拟消息存储
    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    
    @Override
    public void saveMessage(ChatMessage message) {
        if (message != null) {
            messages.add(message);
        }
    }
    
    @Override
    public List<ChatMessage> getUserMessages(String userId, String otherUserId, int limit) {
        if (userId == null || otherUserId == null) {
            return new ArrayList<>();
        }
        
        return messages.stream()
                .filter(msg -> (msg.getSenderId().equals(userId) && msg.getReceiverId().equals(otherUserId)) || 
                               (msg.getSenderId().equals(otherUserId) && msg.getReceiverId().equals(userId)))
                .sorted(Comparator.comparingLong(ChatMessage::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ChatMessage> getRoomMessages(String roomId, int limit) {
        if (roomId == null) {
            return new ArrayList<>();
        }
        
        return messages.stream()
                .filter(msg -> roomId.equals(msg.getRoomId()))
                .sorted(Comparator.comparingLong(ChatMessage::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ChatMessage> getMessagesBetweenUsers(String userId1, String userId2) {
        return getUserMessages(userId1, userId2, 100); // 默认获取最近100条消息
    }
    
    @Override
    public void sendMessage(ChatMessage message) {
        // 保存消息
        saveMessage(message);
        
        // 这里可以添加发送消息的逻辑，例如通过WebSocket发送
        // 在实际实现中，这个方法可能会调用其他服务或组件来发送消息
    }
}


