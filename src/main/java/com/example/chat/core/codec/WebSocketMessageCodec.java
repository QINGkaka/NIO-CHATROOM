package com.example.chat.core.codec;

import com.example.chat.protocol.Message;
import com.example.chat.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;

public class WebSocketMessageCodec extends MessageToMessageCodec<TextWebSocketFrame, Message> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) {
        String json = JsonUtil.toJson(msg);
        out.add(new TextWebSocketFrame(json));
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame frame, List<Object> out) {
        String json = frame.text();
        Message message = JsonUtil.fromJson(json, Message.class);
        out.add(message);
    }
}
