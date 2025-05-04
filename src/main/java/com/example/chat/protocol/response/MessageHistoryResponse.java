package com.example.chat.protocol.response;

import com.example.chat.model.ChatMessage;
import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageHistoryResponse extends AbstractProtocolMessage {
    private List<ChatMessage> messages;
    private boolean hasMore;
    
    public MessageHistoryResponse() {
        super(MessageType.MESSAGE_HISTORY_RESPONSE);
    }
    
    @Builder
    public MessageHistoryResponse(List<ChatMessage> messages, boolean hasMore, String requestId) {
        super(MessageType.MESSAGE_HISTORY_RESPONSE);
        this.messages = messages;
        this.hasMore = hasMore;
        this.setRequestId(requestId);
    }
}





