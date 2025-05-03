package com.example.chat.protocol.request;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageHistoryRequest extends ProtocolMessage {
    private String roomId;
    private long beforeTime; // 查询这个时间点之前的消息
    private int limit;      // 限制返回消息数量
    
    public MessageHistoryRequest() {
        setType(MessageType.MESSAGE_HISTORY_REQUEST);
    }
}
