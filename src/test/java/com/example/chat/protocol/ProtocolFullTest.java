package com.example.chat.protocol;

import static org.junit.jupiter.api.Assertions.*;

import com.example.chat.core.codec.MessageCodec;
import com.example.chat.protocol.request.*;
import com.example.chat.protocol.response.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ProtocolFullTest {
    private Channel serverChannel;
    private Channel clientChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventLoopGroup clientGroup;
    private CountDownLatch messageLatch;
    private ProtocolMessage receivedMessage;

    @BeforeEach
    public void setup() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        clientGroup = new NioEventLoopGroup();
        messageLatch = new CountDownLatch(1);

        // 启动服务器
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline()
                        .addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0))
                        .addLast(new MessageCodec())
                        .addLast(new SimpleChannelInboundHandler<ProtocolMessage>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage msg) {
                                receivedMessage = msg;
                                messageLatch.countDown();
                                
                                // 发送响应
                                if (msg instanceof ChatRequest) {
                                    ChatResponse response = ChatResponse.builder()
                                        .success(true)
                                        .build();
                                    ctx.writeAndFlush(response);
                                }
                            }
                        });
                }
            });
        serverChannel = serverBootstrap.bind(8888).sync().channel();

        // 启动客户端
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(clientGroup)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline()
                        .addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0))
                        .addLast(new MessageCodec());
                }
            });
        clientChannel = clientBootstrap.connect("localhost", 8888).sync().channel();
    }

    @Test
    public void testChatMessage() throws InterruptedException {
        ChatRequest request = ChatRequest.builder()
            .content("Hello World")
            .sender("TestUser")
            .roomId("TestRoom")
            .build();

        clientChannel.writeAndFlush(request);

        assertTrue(messageLatch.await(5, TimeUnit.SECONDS));
        assertNotNull(receivedMessage);
        assertTrue(receivedMessage instanceof ChatRequest);
        assertEquals("Hello World", ((ChatRequest) receivedMessage).getContent());
    }

    @Test
    public void testHeartbeat() throws InterruptedException {
        messageLatch = new CountDownLatch(1);
        HeartbeatRequest request = HeartbeatRequest.builder().build();

        clientChannel.writeAndFlush(request);

        assertTrue(messageLatch.await(5, TimeUnit.SECONDS));
        assertNotNull(receivedMessage);
        assertTrue(receivedMessage instanceof HeartbeatRequest);
    }

    @Test
    public void testRoomOperations() throws InterruptedException {
        messageLatch = new CountDownLatch(1);
        RoomRequest request = RoomRequest.builder()
            .type(MessageType.ROOM_CREATE)
            .roomId("NewRoom")
            .build();

        clientChannel.writeAndFlush(request);

        assertTrue(messageLatch.await(5, TimeUnit.SECONDS));
        assertNotNull(receivedMessage);
        assertTrue(receivedMessage instanceof RoomRequest);
        assertEquals("NewRoom", ((RoomRequest) receivedMessage).getRoomId());
    }

    @AfterEach
    public void teardown() {
        if (clientChannel != null) {
            clientChannel.close();
        }
        if (serverChannel != null) {
            serverChannel.close();
        }
        clientGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
