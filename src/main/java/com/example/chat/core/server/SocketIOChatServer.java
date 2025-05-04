package com.example.chat.core.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.chat.model.ChatMessage;
import com.example.chat.model.User;
import com.example.chat.service.MessageService;
import com.example.chat.service.RoomService;
import com.example.chat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SocketIOChatServer {
    
    @Value("${socketio.host}")
    private String host;
    
    @Value("${socketio.port}")
    private int port;
    
    @Value("${socketio.bossCount}")
    private int bossCount;
    
    @Value("${socketio.workCount}")
    private int workCount;
    
    @Value("${socketio.allowCustomRequests}")
    private boolean allowCustomRequests;
    
    @Value("${socketio.upgradeTimeout}")
    private int upgradeTimeout;
    
    @Value("${socketio.pingTimeout}")
    private int pingTimeout;
    
    @Value("${socketio.pingInterval}")
    private int pingInterval;
    
    private SocketIOServer server;
    
    // 存储用户ID与客户端的映射关系
    private final Map<String, SocketIOClient> userClients = new ConcurrentHashMap<>();
    
    private final UserService userService;
    private final RoomService roomService;
    private final MessageService messageService;
    
    @Autowired
    public SocketIOChatServer(UserService userService, RoomService roomService, MessageService messageService) {
        this.userService = userService;
        this.roomService = roomService;
        this.messageService = messageService;
    }
    
    @PostConstruct
    public void init() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setBossThreads(bossCount);
        config.setWorkerThreads(workCount);
        config.setAllowCustomRequests(allowCustomRequests);
        config.setUpgradeTimeout(upgradeTimeout);
        config.setPingTimeout(pingTimeout);
        config.setPingInterval(pingInterval);
        
        server = new SocketIOServer(config);
        
        // 注册事件监听器
        registerEventListeners();
        
        // 启动服务器
        server.start();
        
        log.info("SocketIO server started on {}:{}", host, port);
    }
    
    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stop();
            log.info("SocketIO server stopped");
        }
    }
    
    private void registerEventListeners() {
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        
        // 注册消息事件监听器
        server.addEventListener("message", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient client, Object data, AckRequest ackRequest) {
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> messageData = (Map<String, Object>) data;
                    handleChatMessage(client, messageData, ackRequest);
                }
            }
        });
        
        // 注册获取在线用户事件监听器
        server.addEventListener("getOnlineUsers", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient client, Object data, AckRequest ackRequest) {
                handleGetOnlineUsers(client, ackRequest);
            }
        });
        
        // 注册加入房间事件监听器
        server.addEventListener("joinRoom", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient client, Object data, AckRequest ackRequest) {
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> roomData = (Map<String, Object>) data;
                    handleJoinRoom(client, roomData, ackRequest);
                }
            }
        });
        
        // 注册离开房间事件监听器
        server.addEventListener("leaveRoom", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient client, Object data, AckRequest ackRequest) {
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> roomData = (Map<String, Object>) data;
                    handleLeaveRoom(client, roomData, ackRequest);
                }
            }
        });
    }
    
    private ConnectListener onConnected() {
        return client -> {
            // 获取认证信息
            String token = client.getHandshakeData().getSingleUrlParam("token");
            
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
                // 移除用户与客户端的关联
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
    private void handleGetOnlineUsers(SocketIOClient client, AckRequest ackRequest) {
        List<User> onlineUsers = userService.getOnlineUsers();
        
        // 转换为客户端期望的格式
        List<Map<String, Object>> userList = onlineUsers.stream()
                .map(this::convertUserToClientFormat)
                .toList();
        
        // 发送响应
        if (ackRequest.isAckRequested()) {
            ackRequest.sendAckData(userList);
        }
    }
    
    // 处理加入房间请求
    private void handleJoinRoom(SocketIOClient client, Map<String, Object> data, AckRequest ackRequest) {
        String userId = client.get("userId");
        if (userId == null) {
            return;
        }
        
        String roomId = (String) data.get("roomId");
        if (roomId == null) {
            return;
        }
        
        // 将客户端加入房间
        client.joinRoom(roomId);
        
        // 更新房间成员
        roomService.addUserToRoom(roomId, userId);
        
        // 发送系统消息通知房间内其他用户
        User user = userService.getUserById(userId);
        ChatMessage joinMessage = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .senderId("system")
                .content(user.getUsername() + " 加入了房间")
                .roomId(roomId)
                .timestamp(System.currentTimeMillis())
                .type(ChatMessage.MessageType.SYSTEM)
                .build();
        
        sendMessageToRoom(roomId, joinMessage);
        
        // 确认加入成功
        if (ackRequest.isAckRequested()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("roomId", roomId);
            ackRequest.sendAckData(response);
        }
    }
    
    // 处理离开房间请求
    private void handleLeaveRoom(SocketIOClient client, Map<String, Object> data, AckRequest ackRequest) {
        String userId = client.get("userId");
        if (userId == null) {
            return;
        }
        
        String roomId = (String) data.get("roomId");
        if (roomId == null) {
            return;
        }
        
        // 将客户端从房间移除
        client.leaveRoom(roomId);
        
        // 更新房间成员
        roomService.removeUserFromRoom(roomId, userId);
        
        // 发送系统消息通知房间内其他用户
        User user = userService.getUserById(userId);
        ChatMessage leaveMessage = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .senderId("system")
                .content(user.getUsername() + " 离开了房间")
                .roomId(roomId)
                .timestamp(System.currentTimeMillis())
                .type(ChatMessage.MessageType.SYSTEM)
                .build();
        
        sendMessageToRoom(roomId, leaveMessage);
        
        // 确认离开成功
        if (ackRequest.isAckRequested()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("roomId", roomId);
            ackRequest.sendAckData(response);
        }
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
        result.put("username", user.getUsername());
        result.put("profilePic", user.getProfilePicture());
        result.put("online", user.isOnline());
        return result;
    }
}






















































