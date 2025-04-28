package com.example.chat.service.impl;

import com.example.chat.dao.RoomDao;
import com.example.chat.model.ChatRoom;
import com.example.chat.service.RoomService;

import java.util.List;
import java.util.UUID;

public class RoomServiceImpl implements RoomService {
    private final RoomDao roomDao;

    public RoomServiceImpl(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    @Override
    public ChatRoom createRoom(String roomName, String creatorId) {
        ChatRoom room = ChatRoom.builder()
            .roomId(UUID.randomUUID().toString())
            .name(roomName)
            .creator(creatorId)
            .createTime(System.currentTimeMillis())
            .build();
        
        room.addMember(creatorId);
        roomDao.save(room);
        return room;
    }

    @Override
    public void deleteRoom(String roomId) {
        roomDao.delete(roomId);
    }

    @Override
    public ChatRoom getRoom(String roomId) {
        return roomDao.findById(roomId);
    }

    @Override
    public List<ChatRoom> getAllRooms() {
        return roomDao.findAll();
    }

    @Override
    public List<ChatRoom> getUserRooms(String userId) {
        return roomDao.findByMember(userId);
    }

    @Override
    public void joinRoom(String roomId, String userId) {
        ChatRoom room = getRoom(roomId);
        if (room != null) {
            room.addMember(userId);
            roomDao.update(room);
        }
    }

    @Override
    public void leaveRoom(String roomId, String userId) {
        ChatRoom room = getRoom(roomId);
        if (room != null) {
            room.removeMember(userId);
            roomDao.update(room);
        }
    }

    @Override
    public boolean isRoomMember(String roomId, String userId) {
        ChatRoom room = getRoom(roomId);
        return room != null && room.getMembers() != null && room.getMembers().contains(userId);
    }
}
