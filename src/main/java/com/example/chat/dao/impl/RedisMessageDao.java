package com.example.chat.dao.impl;

import com.example.chat.dao.MessageDao;
import com.example.chat.model.ChatMessage;
import com.example.chat.util.JsonUtil;
import com.example.chat.util.RedisUtil;
import redis.clients.jedis.Jedis;
import java.util.List;
import java.util.stream.Collectors;

public class RedisMessageDao implements MessageDao {
    private static final String MESSAGE_KEY_PREFIX = "messages:room:";
    private static final String MESSAGE_INDEX_PREFIX = "messages:index:";

    @Override
    public void save(ChatMessage ProtocolMessage) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String json = JsonUtil.toJson(ProtocolMessage);
            String roomKey = MESSAGE_KEY_PREFIX + ProtocolMessage.getRoomId();
            String messageKey = MESSAGE_INDEX_PREFIX + ProtocolMessage.getMessageId();
            
            // 保存消息到时间线
            jedis.zadd(roomKey, ProtocolMessage.getTimestamp(), ProtocolMessage.getMessageId());
            // 保存消息内容
            jedis.set(messageKey, json);
            // 设置消息过期时间（可选，例如30天）
            jedis.expire(messageKey, 30 * 24 * 60 * 60);
        }
    }

    @Override
    public List<ChatMessage> findByRoomId(String roomId, int limit, long beforeTime) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String roomKey = MESSAGE_KEY_PREFIX + roomId;
            List<String> messageIds = jedis.zrevrangeByScore(roomKey, beforeTime, 0, 0, limit);
            
            return messageIds.stream()
                .map(messageId -> jedis.get(MESSAGE_INDEX_PREFIX + messageId))
                .filter(json -> json != null)
                .map(json -> JsonUtil.fromJson(json, ChatMessage.class))
                .collect(Collectors.toList());
        }
    }

    @Override
    public void deleteByRoomId(String roomId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String roomKey = MESSAGE_KEY_PREFIX + roomId;
            // 获取所有消息ID
            List<String> messageIds = jedis.zrange(roomKey, 0, -1);
            
            // 删除所有消息内容
            for (String messageId : messageIds) {
                jedis.del(MESSAGE_INDEX_PREFIX + messageId);
            }
            
            // 删除时间线
            jedis.del(roomKey);
        }
    }

    @Override
    public long getMessageCount(String roomId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String roomKey = MESSAGE_KEY_PREFIX + roomId;
            return jedis.zcard(roomKey);
        }
    }

    @Override
    public List<ChatMessage> searchMessages(String roomId, String keyword, int limit) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String roomKey = MESSAGE_KEY_PREFIX + roomId;
            List<String> messageIds = jedis.zrevrange(roomKey, 0, -1);
            
            return messageIds.stream()
                .map(messageId -> jedis.get(MESSAGE_INDEX_PREFIX + messageId))
                .filter(json -> json != null)
                .map(json -> JsonUtil.fromJson(json, ChatMessage.class))
                .filter(ProtocolMessage -> ProtocolMessage.getContent().contains(keyword))
                .limit(limit)
                .collect(Collectors.toList());
        }
    }

    @Override
    public void deleteMessage(String messageId) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            // 先获取消息内容以获取roomId
            String messageKey = MESSAGE_INDEX_PREFIX + messageId;
            String json = jedis.get(messageKey);
            if (json != null) {
                ChatMessage ProtocolMessage = JsonUtil.fromJson(json, ChatMessage.class);
                String roomKey = MESSAGE_KEY_PREFIX + ProtocolMessage.getRoomId();
                
                // 从时间线中删除
                jedis.zrem(roomKey, messageId);
                // 删除消息内容
                jedis.del(messageKey);
            }
        }
    }

    @Override
    public void updateMessage(ChatMessage ProtocolMessage) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            String messageKey = MESSAGE_INDEX_PREFIX + ProtocolMessage.getMessageId();
            // 检查消息是否存在
            if (!jedis.exists(messageKey)) {
                throw new IllegalArgumentException("ProtocolMessage not found: " + ProtocolMessage.getMessageId());
            }
            
            // 更新消息内容
            String json = JsonUtil.toJson(ProtocolMessage);
            jedis.set(messageKey, json);
            
            // 更新时间线中的时间戳（如果需要）
            String roomKey = MESSAGE_KEY_PREFIX + ProtocolMessage.getRoomId();
            jedis.zadd(roomKey, ProtocolMessage.getTimestamp(), ProtocolMessage.getMessageId());
        }
    }
}
