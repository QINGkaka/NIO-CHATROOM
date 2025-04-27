package com.example.chat.core.server;

import com.example.chat.service.MessageService;
import com.example.chat.service.RoomService;
import com.example.chat.service.UserService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
//Netty服务端启动类（核心入口）
@Slf4j
public class ChatServer {
    private final int port;
    private final UserService userService;
    private final RoomService roomService;
    private final MessageService messageService;
    private Channel serverChannel;

    public ChatServer(int port, UserService userService, RoomService roomService, MessageService messageService) {
        this.port = port;
        this.userService = userService;
        this.roomService = roomService;
        this.messageService = messageService;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChatServerInitializer(userService, roomService, messageService));//初始化ChannelPipeline

            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            log.info("WebSocket server started on port {}", port);
            //等待服务端监听端口关闭
            serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("WebSocket server error", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
    }
}