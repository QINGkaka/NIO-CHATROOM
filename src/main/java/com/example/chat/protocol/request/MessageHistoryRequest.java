package com.example.chat.protocol.request;

import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageHistoryRequest extends AbstractProtocolMessage {
    private String roomId;
    private String userId;
    private long startTime;
    private long endTime;
    private int limit;
    
    public MessageHistoryRequest() {
        super(MessageType.MESSAGE_HISTORY_REQUEST);
    }
    
    @Builder
    public MessageHistoryRequest(String roomId, String userId, long startTime, long endTime, int limit, String requestId) {
        super(MessageType.MESSAGE_HISTORY_REQUEST);
        this.roomId = roomId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.limit = limit;
        this.setRequestId(requestId);
    }
}





