package com.example.chat.service;

import com.example.chat.model.Room;
import java.util.List;
import java.util.Set;

/**
 * 房间服务接口
 */
public interface RoomService {
    
    /**
     * 创建房间
     */
    Room createRoom(String name, String creatorId);
    
    /**
     * 获取房间信息
     */
    Room getRoom(String roomId);
    
    /**
     * 获取所有房间
     */
    List<Room> getAllRooms();
    
    /**
     * 添加用户到房间
     */
    void addUserToRoom(String roomId, String userId);
    
    /**
     * 从房间移除用户
     */
    void removeUserFromRoom(String roomId, String userId);
    
    /**
     * 获取房间中的所有用户
     */
    Set<String> getUsersInRoom(String roomId);
    
    /**
     * 检查用户是否在房间中
     */
    boolean isUserInRoom(String roomId, String userId);
    
    /**
     * 删除房间
     */
    void deleteRoom(String roomId);
    
    /**
     * 获取房间成员列表
     */
    List<String> getRoomMembers(String roomId);
    
    /**
     * 获取用户所在的所有房间
     */
    List<Room> getRoomsByUserId(String userId);
    
    /**
     * 根据ID获取房间
     */
    Room getRoomById(String roomId);
}
