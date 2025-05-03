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
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 1. 验证魔数
        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            throw new DecoderException("Invalid magic number: " + magic);
        }
        
        // 2. 验证版本
        byte version = in.readByte();
        if (version != VERSION) {
            throw new DecoderException("Unsupported version: " + version);
        }
        
        // 3. 读取消息类型
        byte typeCode = in.readByte();
        MessageType type = MessageType.fromCode(typeCode);
        
        // 4. 读取状态码
        short statusCode = in.readShort();
        
        // 5. 读取消息序号
        int requestId = in.readInt();
        
        // 6. 读取消息长度
        int length = in.readInt();
        
        // 7. 读取消息体
        byte[] content = new byte[length];
        in.readBytes(content);
        
        // 8. 反序列化
        String json = new String(content, StandardCharsets.UTF_8);
        ProtocolMessage msg = JsonUtil.fromJson(json, type.getMessageClass());
        msg.setType(type);
        msg.setStatusCode(statusCode);
        msg.setRequestId(String.valueOf(requestId));
        
        out.add(msg);
    }
}


