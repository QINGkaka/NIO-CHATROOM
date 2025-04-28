package com.example.chat.protocol.response;

import com.example.chat.model.ChatMessage;
import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageHistoryResponse extends ProtocolMessage {
    private boolean success;
    private String error;
    private List<ChatMessage> messages;
    private boolean hasMore;    // 是否还有更多历史消息
    
    public MessageHistoryResponse() {
        setType(MessageType.MESSAGE_HISTORY_RESPONSE);
    }
}