package com.example.chat.service.impl;

import com.example.chat.model.Room;
import com.example.chat.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoomServiceImpl implements RoomService {
    
    // 房间ID -> 房间对象
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    // 房间ID -> 用户ID集合
    private final Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();
    
    @Override
    public Room createRoom(String name, String creatorId) {
        String roomId = UUID.randomUUID().toString();
        Room room = Room.builder()
                .id(roomId)
                .name(name)
                .creatorId(creatorId)
                .createTime(System.currentTimeMillis())
                .build();
        
        rooms.put(roomId, room);
        roomUsers.put(roomId, ConcurrentHashMap.newKeySet());
        
        // 创建者自动加入房间
        addUserToRoom(roomId, creatorId);
        
        log.info("Room created: {}, creator: {}", roomId, creatorId);
        return room;
    }
    
    @Override
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }
    
    @Override
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }
    
    @Override
    public void addUserToRoom(String roomId, String userId) {
        Set<String> users = roomUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
        users.add(userId);
        log.info("User {} joined room {}", userId, roomId);
    }
    
    @Override
    public void removeUserFromRoom(String roomId, String userId) {
        Set<String> users = roomUsers.get(roomId);
        if (users != null) {
            users.remove(userId);
            log.info("User {} left room {}", userId, roomId);
        }
    }
    
    @Override
    public Set<String> getUsersInRoom(String roomId) {
        return roomUsers.getOrDefault(roomId, Collections.emptySet());
    }
    
    @Override
    public boolean isUserInRoom(String roomId, String userId) {
        Set<String> users = roomUsers.get(roomId);
        return users != null && users.contains(userId);
    }
    
    @Override
    public void deleteRoom(String roomId) {
        rooms.remove(roomId);
        roomUsers.remove(roomId);
        log.info("Room deleted: {}", roomId);
    }
    
    @Override
    public List<String> getRoomMembers(String roomId) {
        Set<String> users = roomUsers.get(roomId);
        if (users != null) {
            return new ArrayList<>(users);
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<Room> getRoomsByUserId(String userId) {
        return rooms.values().stream()
                .filter(room -> isUserInRoom(room.getId(), userId))
                .collect(Collectors.toList());
    }
    
    @Override
    public Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }
}














