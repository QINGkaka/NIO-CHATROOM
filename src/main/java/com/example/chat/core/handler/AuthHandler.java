package com.example.chat.core.handler;

import com.example.chat.protocol.Message;
import com.example.chat.protocol.MessageType;
import com.example.chat.protocol.response.ErrorResponse;
import com.example.chat.protocol.response.LoginResponse;
import com.example.chat.service.UserService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
//业务处理器示例（用户认证）
@Slf4j
@ChannelHandler.Sharable
public class AuthHandler extends SimpleChannelInboundHandler<Message> {
    private final UserService userService;
    
    public AuthHandler(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg.getType() != MessageType.LOGIN) {
            if (isAuthenticated(ctx)) {
                ctx.fireChannelRead(msg); // 非登录消息透传
            } else {
                ErrorResponse errorResponse = ErrorResponse.builder()
                    .type(MessageType.ERROR)
                    .content("Please login first")
                    .build();
                ctx.writeAndFlush(errorResponse);
                ctx.close();
            }
            return;
        }
        //处理登录消息
        try {
            boolean authenticated = userService.authenticate(msg.getSender(), msg.getContent());
            if (authenticated) {
                ctx.channel().attr(AttributeKey.valueOf("userId")).set(msg.getSender());
                LoginResponse loginResponse = LoginResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .build();
                ctx.writeAndFlush(loginResponse);
            } else {
                ErrorResponse errorResponse = ErrorResponse.builder()
                    .type(MessageType.ERROR)
                    .content("Invalid credentials")
                    .build();
                ctx.writeAndFlush(errorResponse);
                ctx.close();
            }
        } catch (Exception e) {
            log.error("Authentication error", e);
            ctx.close();
        }
    }
    
    private boolean isAuthenticated(ChannelHandlerContext ctx) {
        return ctx.channel().hasAttr(AttributeKey.valueOf("userId"));
    }
}

