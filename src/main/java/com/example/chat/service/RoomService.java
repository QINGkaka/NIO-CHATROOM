package com.example.chat.service;

import com.example.chat.model.ChatRoom;
import java.util.List;

public interface RoomService {
    ChatRoom createRoom(String roomName, String creatorId);
    void deleteRoom(String roomId);
    ChatRoom getRoom(String roomId);
    List<ChatRoom> getAllRooms();
    List<ChatRoom> getUserRooms(String userId);
    void joinRoom(String roomId, String userId);
    void leaveRoom(String roomId, String userId);
    boolean isRoomMember(String roomId, String userId);
}
