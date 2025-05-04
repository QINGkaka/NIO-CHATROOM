package com.example.chat.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.DecoderException;
import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import com.example.chat.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageCodec extends MessageToMessageCodec<ByteBuf, ProtocolMessage> {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private static final byte VERSION = 1;
    
    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        try {
            // 1. 魔数 4字节
            buf.writeInt(MAGIC_NUMBER);
            
            // 2. 版本号 1字节
            buf.writeByte(VERSION);
            
            // 3. 序列化方式 1字节
            buf.writeByte(SerializerType.JSON.ordinal());
            
            // 4. 消息类型 1字节
            buf.writeByte(msg.getType().ordinal());
            
            // 5. 状态码 2字节
            buf.writeShort(msg.getStatusCode());
            
            // 6. 请求ID 4字节
            buf.writeInt(msg.getRequestId().hashCode());
            
            // 7. 正文长度 4字节
            byte[] bytes = JsonUtil.toJson(msg).getBytes(StandardCharsets.UTF_8);
            buf.writeInt(bytes.length);
            
            // 8. 消息正文
            buf.writeBytes(bytes);
            
            out.add(buf);
        } catch (Exception e) {
            buf.release();
            throw e;
        }
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 保存读取索引，以便需要时重置
        in.markReaderIndex();
        
        // 确保有足够的字节可读
        if (in.readableBytes() < 4) {
            return;
        }
        
        // 1. 魔数 4字节
        int magicNum = in.readInt();
        if (magicNum != MAGIC_NUMBER) {
            in.resetReaderIndex();
            throw new DecoderException("Invalid magic number: " + magicNum);
        }
        
        // 确保有足够的字节可读
        if (in.readableBytes() < 1 + 1 + 1 + 2 + 4 + 4) {
            in.resetReaderIndex();
            return;
        }
        
        // 2. 版本号 1字节
        byte version = in.readByte();
        if (version != VERSION) {
            in.resetReaderIndex();
            throw new DecoderException("Version not supported: " + version);
        }
        
        // 3. 序列化方式 1字节 (暂时只支持JSON)
        in.readByte(); // 跳过序列化类型
        
        // 4. 消息类型 1字节
        byte messageTypeCode = in.readByte();
        MessageType messageType = MessageType.fromCode(messageTypeCode);
        if (messageType == null) {
            in.resetReaderIndex();
            throw new DecoderException("Unknown message type: " + messageTypeCode);
        }
        
        // 5. 状态码 2字节 (在反序列化时设置)
        short statusCode = in.readShort();
        
        // 6. 请求ID 4字节 (在反序列化时设置)
        int requestIdHash = in.readInt();
        String requestId = String.valueOf(requestIdHash);
        ProtocolMessage message = new ProtocolMessage();
        message.setRequestId(requestId);  // 使用 requestId
        
        // 7. 正文长度 4字节
        int length = in.readInt();
        if (in.readableBytes() < length) {
            // 数据不完整，重置读指针
            in.resetReaderIndex();
            return;
        }
        
        // 8. 消息正文
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        
        // 反序列化为基本的ProtocolMessage
        String json = new String(bytes, StandardCharsets.UTF_8);
        ProtocolMessage decodedMessage = JsonUtil.fromJson(json, ProtocolMessage.class);
        
        // 设置从头部读取的值
        decodedMessage.setType(messageType);
        decodedMessage.setStatusCode(statusCode);
        
        out.add(decodedMessage);
    }
}















