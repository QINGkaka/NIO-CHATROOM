package com.example.chat.core.server;

import com.example.chat.model.ChatMessage;
import com.example.chat.service.MessageService;
import com.example.chat.service.RoomService;
import com.example.chat.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatServer implements ChatServer {

    private final UserService userService;
    private final RoomService roomService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    
    @Value("${websocket.port:8080}")
    private int port;
    
    @Value("${websocket.path:/ws}")
    private String websocketPath;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    // 存储用户ID和Channel的映射
    private final Map<String, Channel> userChannels = new ConcurrentHashMap<>();
    
    @Override
    @PostConstruct
    public void start() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // HTTP协议编解码器
                            pipeline.addLast(new HttpServerCodec());
                            // 支持大数据流
                            pipeline.addLast(new ChunkedWriteHandler());
                            // 聚合HTTP消息
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            // WebSocket协议处理
                            pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
                            // 自定义业务处理器
                            pipeline.addLast(new WebSocketFrameHandler());
                        }
                    });
            
            serverChannel = bootstrap.bind(port).sync().channel();
            log.info("WebSocket server started on port {}", port);
            
        } catch (Exception e) {
            log.error("Failed to start WebSocket server", e);
            throw e;
        }
    }
    
    @PreDestroy
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("WebSocket server stopped");
    }
    
    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(String userId, ChatMessage message) {
        Channel channel = userChannels.get(userId);
        if (channel != null && channel.isActive()) {
            try {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "message");
                response.put("id", message.getId());
                response.put("senderId", message.getSenderId());
                response.put("content", message.getContent());
                response.put("timestamp", message.getTimestamp());
                
                if (message.getType() == ChatMessage.MessageType.IMAGE) {
                    response.put("image", message.getContent());
                    response.put("text", null);
                } else {
                    response.put("text", message.getContent());
                }
                
                String json = objectMapper.writeValueAsString(response);
                channel.writeAndFlush(new TextWebSocketFrame(json));
                log.debug("Message sent to user {}: {}", userId, json);
            } catch (Exception e) {
                log.error("Failed to send message to user " + userId, e);
            }
        } else {
            log.debug("User {} is offline, message queued", userId);
            // 可以实现离线消息存储逻辑
        }
    }
    
    /**
     * 发送消息到聊天室
     */
    public void sendMessageToRoom(String roomId, ChatMessage message) {
        try {
            // 获取房间内的所有用户
            roomService.getUsersInRoom(roomId).forEach(userId -> {
                sendMessageToUser(userId, message);
            });
            log.debug("Message sent to room {}", roomId);
        } catch (Exception e) {
            log.error("Failed to send message to room " + roomId, e);
        }
    }
    
    /**
     * WebSocket帧处理器
     */
    private class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
            String text = frame.text();
            log.debug("Received message: {}", text);
            
            try {
                // 解析消息
                @SuppressWarnings("unchecked")
                Map<String, Object> message = objectMapper.readValue(text, Map.class);
                String type = (String) message.get("type");
                
                switch (type) {
                    case "auth":
                        handleAuth(ctx, message);
                        break;
                    case "message":
                        handleMessage(ctx, message);
                        break;
                    case "join":
                        handleJoin(ctx, message);
                        break;
                    case "leave":
                        handleLeave(ctx, message);
                        break;
                    default:
                        log.warn("Unknown message type: {}", type);
                }
            } catch (Exception e) {
                log.error("Error processing message", e);
                sendError(ctx, "Invalid message format");
            }
        }
        
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            log.debug("Handler added: {}", ctx.channel().id());
        }
        
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            log.debug("Handler removed: {}", ctx.channel().id());
            // 用户断开连接，从映射中移除
            userChannels.entrySet().removeIf(entry -> entry.getValue().equals(ctx.channel()));
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("WebSocket error", cause);
            ctx.close();
        }
        
        /**
         * 处理认证消息
         */
        private void handleAuth(ChannelHandlerContext ctx, Map<String, Object> message) {
            String userId = (String) message.get("userId");
            String authToken = (String) message.get("token");
            
            // 这里应该验证token，但为了简化，我们直接接受
            // 在实际项目中，应该调用userService验证token
            String validUserId = userService.validateToken(authToken);
            
            if (validUserId != null) {
                // 将用户ID与Channel关联
                userChannels.put(userId, ctx.channel());
                
                // 发送认证成功响应
                Map<String, Object> response = new HashMap<>();
                response.put("type", "auth");
                response.put("success", true);
                response.put("userId", userId);
                
                try {
                    String json = objectMapper.writeValueAsString(response);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
                    log.debug("User authenticated: {}", userId);
                } catch (Exception e) {
                    log.error("Failed to send auth response", e);
                }
            } else {
                sendError(ctx, "Authentication failed");
                ctx.close();
            }
        }
        
        /**
         * 处理聊天消息
         */
        private void handleMessage(ChannelHandlerContext ctx, Map<String, Object> message) {
            String userId = getUserIdFromChannel(ctx.channel());
            if (userId == null) {
                sendError(ctx, "Not authenticated");
                return;
            }
            
            String text = (String) message.get("text");
            String image = (String) message.get("image");
            String roomId = (String) message.get("roomId");
            String receiverId = (String) message.get("receiverId");
            
            // 创建消息对象
            ChatMessage chatMessage = ChatMessage.builder()
                    .senderId(userId)
                    .content(text != null ? text : image)
                    .roomId(roomId)
                    .receiverId(receiverId)
                    .timestamp(System.currentTimeMillis())
                    .type(image != null ? ChatMessage.MessageType.IMAGE : ChatMessage.MessageType.TEXT)
                    .build();
            
            // 保存消息
            messageService.saveMessage(chatMessage);
            
            // 发送消息
            if (roomId != null && !roomId.isEmpty()) {
                sendMessageToRoom(roomId, chatMessage);
            } else if (receiverId != null && !receiverId.isEmpty()) {
                sendMessageToUser(receiverId, chatMessage);
            }
            
            // 发送确认
            Map<String, Object> response = new HashMap<>();
            response.put("type", "messageAck");
            response.put("messageId", chatMessage.getId());
            response.put("success", true);
            
            try {
                String json = objectMapper.writeValueAsString(response);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                log.error("Failed to send message ack", e);
            }
        }
        
        /**
         * 处理加入房间消息
         */
        private void handleJoin(ChannelHandlerContext ctx, Map<String, Object> message) {
            String userId = getUserIdFromChannel(ctx.channel());
            if (userId == null) {
                sendError(ctx, "Not authenticated");
                return;
            }
            
            String roomId = (String) message.get("roomId");
            
            // 加入房间
            roomService.addUserToRoom(roomId, userId);
            
            // 发送确认
            Map<String, Object> response = new HashMap<>();
            response.put("type", "joinAck");
            response.put("roomId", roomId);
            response.put("success", true);
            
            try {
                String json = objectMapper.writeValueAsString(response);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
                
                // 通知房间其他用户
                ChatMessage systemMessage = ChatMessage.builder()
                        .senderId("system")
                        .content(userService.getUserById(userId).getUsername() + " joined the room")
                        .roomId(roomId)
                        .timestamp(System.currentTimeMillis())
                        .type(ChatMessage.MessageType.SYSTEM)
                        .build();
                
                sendMessageToRoom(roomId, systemMessage);
            } catch (Exception e) {
                log.error("Failed to send join ack", e);
            }
        }
        
        /**
         * 处理离开房间消息
         */
        private void handleLeave(ChannelHandlerContext ctx, Map<String, Object> message) {
            String userId = getUserIdFromChannel(ctx.channel());
            if (userId == null) {
                sendError(ctx, "Not authenticated");
                return;
            }
            
            String roomId = (String) message.get("roomId");
            
            // 离开房间
            roomService.removeUserFromRoom(roomId, userId);
            
            // 发送确认
            Map<String, Object> response = new HashMap<>();
            response.put("type", "leaveAck");
            response.put("roomId", roomId);
            response.put("success", true);
            
            try {
                String json = objectMapper.writeValueAsString(response);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
                
                // 通知房间其他用户
                ChatMessage systemMessage = ChatMessage.builder()
                        .senderId("system")
                        .content(userService.getUserById(userId).getUsername() + " left the room")
                        .roomId(roomId)
                        .timestamp(System.currentTimeMillis())
                        .type(ChatMessage.MessageType.SYSTEM)
                        .build();
                
                sendMessageToRoom(roomId, systemMessage);
            } catch (Exception e) {
                log.error("Failed to send leave ack", e);
            }
        }
        
        /**
         * 发送错误消息
         */
        private void sendError(ChannelHandlerContext ctx, String errorMessage) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "error");
            response.put("message", errorMessage);
            
            try {
                String json = objectMapper.writeValueAsString(response);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                log.error("Failed to send error message", e);
            }
        }
        
        /**
         * 从Channel获取用户ID
         */
        private String getUserIdFromChannel(Channel channel) {
            for (Map.Entry<String, Channel> entry : userChannels.entrySet()) {
                if (entry.getValue().equals(channel)) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }
}





