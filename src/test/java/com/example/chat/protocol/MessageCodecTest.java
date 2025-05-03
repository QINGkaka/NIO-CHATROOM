package com.example.chat.protocol;

import com.example.chat.core.codec.MessageCodec;
import com.example.chat.protocol.request.ChatRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.DecoderException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MessageCodecTest {

    @Test
    public void testEncodeAndDecode() {
        // 1. 创建测试用的EmbeddedChannel
        EmbeddedChannel channel = new EmbeddedChannel(
            new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
            new MessageCodec()
        );

        // 2. 创建测试消息
        ChatRequest original = ChatRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .sender("testUser")
            .content("Hello, World!")
            .roomId("room1")
            .build();

        // 3. 写入消息（触发编码）
        assertTrue(channel.writeOutbound(original));
        
        // 4. 读取编码后的ByteBuf
        ByteBuf encoded = channel.readOutbound();
        
        // 5. 写入ByteBuf（触发解码）
        assertTrue(channel.writeInbound(encoded));
        
        // 6. 读取解码后的消息
        ProtocolMessage decoded = channel.readInbound();
        
        // 7. 验证解码后的消息
        assertNotNull(decoded);
        assertTrue(decoded instanceof ChatRequest);
        ChatRequest decodedRequest = (ChatRequest) decoded;
        
        assertEquals(original.getRequestId(), decodedRequest.getRequestId());
        assertEquals(original.getSender(), decodedRequest.getSender());
        assertEquals(original.getContent(), decodedRequest.getContent());
        assertEquals(original.getRoomId(), decodedRequest.getRoomId());
    }

    @Test
    public void testMagicNumber() {
        EmbeddedChannel channel = new EmbeddedChannel(
            new MessageCodec()
        );
        
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            // 写入错误的魔数
            buf.writeInt(0x12345678); // 使用不同的魔数
            buf.writeByte(1); // 版本
            buf.writeByte(0); // 序列化方式
            buf.writeByte(0); // 消息类型
            buf.writeInt(1); // 请求ID
            buf.writeInt(0); // 消息长度
            
            assertThrows(DecoderException.class, () -> {
                channel.writeInbound(buf);
            });
        } finally {
            buf.release();
            channel.close();
        }
    }
}
