package com.example.chat.dao;

import com.example.chat.model.ChatRoom;
import java.util.List;

public interface RoomDao {
    ChatRoom findById(String roomId);
    void save(ChatRoom room);
    void update(ChatRoom room);
    void delete(String roomId);
    List<ChatRoom> findAll();
    List<ChatRoom> findByMember(String userId);
}
