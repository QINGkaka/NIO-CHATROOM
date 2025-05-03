package com.example.chat.core.handler;

import com.example.chat.model.ChatMessage;
import com.example.chat.model.ChatRoom;
import com.example.chat.model.User;
import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.StatusCode;
import com.example.chat.protocol.request.ChatRequest;
import com.example.chat.protocol.request.LoginRequest;
import com.example.chat.protocol.request.RoomRequest;
import com.example.chat.protocol.response.ChatResponse;
import com.example.chat.protocol.response.LoginResponse;
import com.example.chat.protocol.response.RoomResponse;
import com.example.chat.protocol.response.ErrorResponse;
import com.example.chat.protocol.MessageValidator;
import com.example.chat.service.MessageService;
import com.example.chat.service.RoomService;
import com.example.chat.service.UserService;
import com.example.chat.util.ChannelUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;


@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<ProtocolMessage> {
    private final UserService userService;
    private final RoomService roomService;
    private final MessageService messageService;

    public WebSocketServerHandler(UserService userService, RoomService roomService, MessageService messageService) {
        this.userService = userService;
        this.roomService = roomService;
        this.messageService = messageService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage msg) {
        try {
            // 添加消息验证
            MessageValidator.validate(msg);
            
            // 根据消息类型处理
            switch (msg.getType()) {
                case LOGIN_REQUEST:
                    handleLoginRequest(ctx, (LoginRequest) msg);
                    break;
                case CHAT_REQUEST:
                    handleChatRequest(ctx, (ChatRequest) msg);
                    break;
                case ROOM_CREATE:
                case ROOM_JOIN:
                case ROOM_LEAVE:
                case ROOM_LIST:
                    handleRoomRequest(ctx, (RoomRequest) msg);
                    break;
                default:
                    log.warn("Unsupported message type: {}", msg.getType());
                    ErrorResponse errorResponse = new ErrorResponse(
                        StatusCode.BAD_REQUEST,
                        "Unsupported message type"
                    );
                    ctx.writeAndFlush(errorResponse);
            }
        } catch (IllegalArgumentException e) {
            // 处理验证失败的情况
            log.error("Message validation failed: {}", e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse(
                StatusCode.BAD_REQUEST,
                e.getMessage()
            );
            ctx.writeAndFlush(errorResponse);
        } catch (Exception e) {
            // 处理其他异常
            log.error("Error processing message", e);
            ErrorResponse errorResponse = new ErrorResponse(
                StatusCode.INTERNAL_ERROR,
                "Internal server error"
            );
            ctx.writeAndFlush(errorResponse);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            log.info("WebSocket client connected: {}", ctx.channel().remoteAddress());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String userId = ChannelUtil.getUserId(ctx.channel());
        if (userId != null) {
            userService.setUserOnlineStatus(userId, false);
            ChannelUtil.unbindUser(ctx.channel());
            log.info("User disconnected: {}", userId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket handler error", cause);
        ctx.close();
    }

    private void handleLoginRequest(ChannelHandlerContext ctx, LoginRequest request) {
        LoginResponse response = new LoginResponse();
        response.setRequestId(request.getRequestId());

        try {
            User user = userService.login(request.getUsername(), request.getPassword());
            if (user != null) {
                ChannelUtil.bindUser(ctx.channel(), user.getUserId());
                userService.setUserOnlineStatus(user.getUserId(), true);
                response.setSuccess(true);
                response.setUser(user);
            } else {
                response.setSuccess(false);
                response.setError("Invalid username or password");
            }
        } catch (Exception e) {
            log.error("Login error", e);
            response.setSuccess(false);
            response.setError("Internal server error");
        }

        ctx.writeAndFlush(response);
    }

    private void handleChatRequest(ChannelHandlerContext ctx, ChatRequest request) {
        ChatResponse response = new ChatResponse();
        response.setRequestId(request.getRequestId());

        String userId = ChannelUtil.getUserId(ctx.channel());
        if (userId == null) {
            response.setSuccess(false);
            response.setError("Not logged in");
            ctx.writeAndFlush(response);
            return;
        }

        try {
            if (!roomService.isRoomMember(request.getRoomId(), userId)) {
                response.setSuccess(false);
                response.setError("Not a member of the room");
                ctx.writeAndFlush(response);
                return;
            }

            User user = userService.getUserById(userId);
            ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .roomId(request.getRoomId())
                .userId(userId)
                .username(user.getUsername())
                .content(request.getContent())
                .timestamp(System.currentTimeMillis())
                .type(ChatMessage.MessageType.TEXT)
                .build();

            messageService.sendMessage(message);
            messageService.broadcastMessage(message);

            response.setSuccess(true);
            response.setMessage(message);  // 从 setProtocolMessage 改为 setMessage
        } catch (Exception e) {
            log.error("Chat error", e);
            ErrorResponse errorResponse = new ErrorResponse(
                StatusCode.INTERNAL_ERROR,
                "Failed to process chat message"
            );
            ctx.writeAndFlush(errorResponse);
            return;
        }

        ctx.writeAndFlush(response);
    }

    private void handleRoomRequest(ChannelHandlerContext ctx, RoomRequest request) {
        RoomResponse response = new RoomResponse();
        response.setRequestId(request.getRequestId());

        String userId = ChannelUtil.getUserId(ctx.channel());
        if (userId == null) {
            response.setSuccess(false);
            response.setError("Not logged in");
            ctx.writeAndFlush(response);
            return;
        }

        try {
            switch (request.getAction().toUpperCase()) {
                case "CREATE":
                    ChatRoom room = roomService.createRoom(request.getRoomName(), userId);
                    response.setSuccess(true);
                    response.setRoom(room);
                    break;

                case "JOIN":
                    roomService.joinRoom(request.getRoomId(), userId);
                    response.setSuccess(true);
                    response.setRoom(roomService.getRoom(request.getRoomId()));
                    break;

                case "LEAVE":
                    roomService.leaveRoom(request.getRoomId(), userId);
                    response.setSuccess(true);
                    break;

                default:
                    response.setSuccess(false);
                    response.setError("Unknown action: " + request.getAction());
            }
        } catch (Exception e) {
            log.error("Room operation error", e);
            ErrorResponse errorResponse = new ErrorResponse(
                StatusCode.ROOM_NOT_EXIST,
                "Room operation failed"
            );
            ctx.writeAndFlush(errorResponse);
            return;
        }

        ctx.writeAndFlush(response);
    }
}















