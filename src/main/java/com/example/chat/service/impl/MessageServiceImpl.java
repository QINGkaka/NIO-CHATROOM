package com.example.chat.service.impl;

import com.example.chat.dao.MessageDao;
import com.example.chat.dao.RoomDao;
import com.example.chat.model.ChatMessage;
import com.example.chat.model.ChatRoom;
import com.example.chat.service.MessageService;
import com.example.chat.util.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import com.example.chat.util.JsonUtil;

import java.util.List;
import java.util.UUID;

public class MessageServiceImpl implements MessageService {
    private final MessageDao messageDao;
    private final RoomDao roomDao;

    public MessageServiceImpl(MessageDao messageDao, RoomDao roomDao) {
        this.messageDao = messageDao;
        this.roomDao = roomDao;
    }

    @Override
    public void sendMessage(ChatMessage ProtocolMessage) {
        if (ProtocolMessage.getMessageId() == null) {
            ProtocolMessage.setMessageId(UUID.randomUUID().toString());
        }
        if (ProtocolMessage.getTimestamp() == 0) {
            ProtocolMessage.setTimestamp(System.currentTimeMillis());
        }
        messageDao.save(ProtocolMessage);
    }

    @Override
    public List<ChatMessage> getRoomMessages(String roomId, int limit, long beforeTime) {
        return messageDao.findByRoomId(roomId, limit, beforeTime);
    }

    @Override
    public void deleteRoomMessages(String roomId) {
        messageDao.deleteByRoomId(roomId);
    }

    @Override
    public void broadcastMessage(ChatMessage ProtocolMessage) {
        String messageJson = JsonUtil.toJson(ProtocolMessage);
        TextWebSocketFrame frame = new TextWebSocketFrame(messageJson);
        
        if (ProtocolMessage.getRoomId() != null) {
            ChatRoom room = roomDao.findById(ProtocolMessage.getRoomId());
            if (room != null && room.getMembers() != null) {
                for (String userId : room.getMembers()) {
                    Channel channel = ChannelUtil.getChannel(userId);
                    if (channel != null && channel.isActive()) {
                        channel.writeAndFlush(frame.retain());
                    }
                }
            }
        }
        frame.release();
    }

    @Override
    public long getMessageCount(String roomId) {
        return messageDao.getMessageCount(roomId);
    }

    @Override
    public List<ChatMessage> searchMessages(String roomId, String keyword, int limit) {
        return messageDao.searchMessages(roomId, keyword, limit);
    }

    @Override
    public void deleteMessage(String messageId) {
        messageDao.deleteMessage(messageId);
    }

    @Override
    public void updateMessage(ChatMessage ProtocolMessage) {
        if (ProtocolMessage.getMessageId() == null) {
            throw new IllegalArgumentException("ProtocolMessage ID cannot be null for update");
        }
        messageDao.updateMessage(ProtocolMessage);
    }
}

