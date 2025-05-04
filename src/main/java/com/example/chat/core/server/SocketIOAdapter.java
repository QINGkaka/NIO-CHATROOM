package com.example.chat.core.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.chat.model.ChatMessage;
import com.example.chat.model.User;
import com.example.chat.service.MessageService;
import com.example.chat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SocketIOAdapter {
    
    private final SocketIOServer server;
    private final UserService userService;
    private final MessageService messageService;
    
    // 存储用户ID和客户端的映射关系
    private final Map<String, SocketIOClient> userClients = new ConcurrentHashMap<>();
    
    @Autowired
    public SocketIOAdapter(SocketIOServer server, UserService userService, MessageService messageService) {
        this.server = server;
        this.userService = userService;
        this.messageService = messageService;
        
        // 注册事件监听器
        this.server.addConnectListener(onConnected());
        this.server.addDisconnectListener(onDisconnected());
        
        // 注册消息处理事件 - 与chat-app兼容的事件名称
        this.server.addEventListener("sendMessage", Map.class, 
            (client, data, ack) -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> messageData = (Map<String, Object>) data;
                handleChatMessage(client, messageData, ack);
            });
        this.server.addEventListener("getOnlineUsers", Object.class, 
            (client, data, ack) -> handleGetOnlineUsers(client, data, ack));
    }
    
    @PostConstruct
    public void start() {
        server.start();
        log.info("Socket.IO server started on {}:{}", 
                server.getConfiguration().getHostname(), 
                server.getConfiguration().getPort());
    }
    
    @PreDestroy
    public void stop() {
        server.stop();
        log.info("Socket.IO server stopped");
    }
    
    private ConnectListener onConnected() {
        return client -> {
            // 获取认证信息 - 兼容chat-app的token格式
            String token = client.getHandshakeData().getSingleUrlParam("token");
            if (token == null) {
                // 尝试从headers中获取
                token = client.getHandshakeData().getHttpHeaders().get("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
            }
            
            // 验证token并获取用户ID
            String userId = userService.validateToken(token);
            if (userId != null) {
                // 将用户ID与客户端关联
                client.set("userId", userId);
                userClients.put(userId, client);
                
                // 更新用户在线状态
                User user = userService.getUserById(userId);
                if (user != null) {
                    user.setOnline(true);
                    userService.updateUser(user);
                }
                
                // 广播用户上线状态
                broadcastUserStatus(userId, true);
                
                log.info("Client connected: {}, userId: {}", client.getSessionId(), userId);
            } else {
                // 认证失败，断开连接
                client.disconnect();
                log.warn("Authentication failed for client: {}", client.getSessionId());
            }
        };
    }
    
    private DisconnectListener onDisconnected() {
        return client -> {
            String userId = client.get("userId");
            if (userId != null) {
                // 移除客户端映射
                userClients.remove(userId);
                
                // 更新用户在线状态
                User user = userService.getUserById(userId);
                if (user != null) {
                    user.setOnline(false);
                    userService.updateUser(user);
                }
                
                // 广播用户下线状态
                broadcastUserStatus(userId, false);
                
                log.info("Client disconnected: {}, userId: {}", client.getSessionId(), userId);
            }
        };
    }
    
    // 处理聊天消息
    private void handleChatMessage(SocketIOClient client, Map<String, Object> data, AckRequest ackRequest) {
        String userId = client.get("userId");
        if (userId == null) {
            return;
        }
        
        String text = (String) data.get("text");
        String image = (String) data.get("image");
        String receiverId = (String) data.get("receiverId");
        String roomId = (String) data.get("roomId");
        
        // 创建消息对象
        ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .senderId(userId)
                .content(text != null ? text : image)
                .receiverId(receiverId)
                .roomId(roomId)
                .timestamp(System.currentTimeMillis())
                .type(image != null ? ChatMessage.MessageType.IMAGE : ChatMessage.MessageType.TEXT)
                .build();
        
        // 保存消息
        messageService.saveMessage(message);
        
        // 发送消息给接收者或房间
        if (roomId != null) {
            sendMessageToRoom(roomId, message);
        } else if (receiverId != null) {
            sendMessageToUser(receiverId, message);
        }
        
        // 确认消息已收到
        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(convertToClientFormat(message));
        }
    }
    
    // 处理获取在线用户请求
    private void handleGetOnlineUsers(SocketIOClient client, Object data, AckRequest ackRequest) {
        List<User> onlineUsers = userService.getOnlineUsers();
        
        // 转换为客户端期望的格式
        List<Map<String, Object>> userList = onlineUsers.stream()
                .map(this::convertUserToClientFormat)
                .collect(Collectors.toList());
        
        // 发送响应
        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(userList);
        }
        
        // 也可以广播给所有客户端
        server.getBroadcastOperations().sendEvent("getOnlineUsers", 
            (Object) userClients.keySet().toArray(new String[0]));
    }
    
    // 广播用户状态变更
    private void broadcastUserStatus(String userId, boolean online) {
        User user = userService.getUserById(userId);
        if (user != null) {
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("userId", userId);
            statusData.put("username", user.getUsername());
            statusData.put("online", online);
            
            server.getBroadcastOperations().sendEvent("userStatus", statusData);
        }
    }
    
    // 发送消息给指定用户
    public void sendMessageToUser(String userId, ChatMessage message) {
        SocketIOClient client = userClients.get(userId);
        if (client != null) {
            // 转换为客户端期望的格式
            Map<String, Object> messageData = convertToClientFormat(message);
            
            // 发送新消息事件
            client.sendEvent("newMessage", messageData);
        }
    }
    
    // 发送消息给房间内所有用户
    public void sendMessageToRoom(String roomId, ChatMessage message) {
        // 转换为客户端期望的格式
        Map<String, Object> messageData = convertToClientFormat(message);
        
        // 发送给房间内所有客户端
        server.getRoomOperations(roomId).sendEvent("newMessage", messageData);
    }
    
    // 将消息转换为客户端期望的格式
    private Map<String, Object> convertToClientFormat(ChatMessage message) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", message.getId());
        result.put("senderId", message.getSenderId());
        
        // 根据消息类型设置不同的字段
        if (message.getType() == ChatMessage.MessageType.TEXT) {
            result.put("text", message.getContent());
        } else if (message.getType() == ChatMessage.MessageType.IMAGE) {
            result.put("image", message.getContent());
        } else {
            result.put("text", message.getContent());
        }
        
        result.put("timestamp", message.getTimestamp());
        
        if (message.getReceiverId() != null) {
            result.put("receiverId", message.getReceiverId());
        }
        
        if (message.getRoomId() != null) {
            result.put("roomId", message.getRoomId());
        }
        
        return result;
    }
    
    // 将用户对象转换为客户端期望的格式
    private Map<String, Object> convertUserToClientFormat(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("_id", user.getId());
        result.put("fullName", user.getFullName());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("profilePic", user.getProfilePicture());
        result.put("online", user.isOnline());
        return result;
    }
}


