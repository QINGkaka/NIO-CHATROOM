package com.example.chat.dao.impl;

import com.example.chat.dao.RoomDao;
import com.example.chat.model.ChatRoom;
import com.example.chat.util.JsonUtil;
import com.example.chat.util.RedisUtil;
import redis.clients.jedis.Jedis;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisRoomDao implements RoomDao {
    private static final String ROOM_KEY_PREFIX = "room:";
    private static final String ROOM_MEMBER_KEY_PREFIX = "room:members:";
    private static final String USER_ROOMS_KEY_PREFIX = "user:rooms:";

    @Override
    public ChatRoom findById(String roomId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String json = jedis.get(ROOM_KEY_PREFIX + roomId);
            return json != null ? JsonUtil.fromJson(json, ChatRoom.class) : null;
        }
    }

    @Override
    public void save(ChatRoom room) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String json = JsonUtil.toJson(room);
            jedis.set(ROOM_KEY_PREFIX + room.getRoomId(), json);
            
            // Save room members
            String memberKey = ROOM_MEMBER_KEY_PREFIX + room.getRoomId();
            if (room.getMembers() != null) {
                String[] members = room.getMembers().toArray(new String[0]);
                jedis.sadd(memberKey, members);
                
                // Update user-room relationships
                for (String memberId : members) {
                    jedis.sadd(USER_ROOMS_KEY_PREFIX + memberId, room.getRoomId());
                }
            }
        }
    }

    @Override
    public void update(ChatRoom room) {
        save(room);
    }

    @Override
    public void delete(String roomId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            // Remove room members
            String memberKey = ROOM_MEMBER_KEY_PREFIX + roomId;
            Set<String> members = jedis.smembers(memberKey);
            for (String memberId : members) {
                jedis.srem(USER_ROOMS_KEY_PREFIX + memberId, roomId);
            }
            
            jedis.del(memberKey);
            jedis.del(ROOM_KEY_PREFIX + roomId);
        }
    }

    @Override
    public List<ChatRoom> findAll() {
        try (Jedis jedis = RedisUtil.getJedis()) {
            Set<String> keys = jedis.keys(ROOM_KEY_PREFIX + "*");
            return keys.stream()
                .map(key -> jedis.get(key))
                .map(json -> JsonUtil.fromJson(json, ChatRoom.class))
                .collect(Collectors.toList());
        }
    }

    @Override
    public List<ChatRoom> findByMember(String userId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            Set<String> roomIds = jedis.smembers(USER_ROOMS_KEY_PREFIX + userId);
            return roomIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
        }
    }
}
