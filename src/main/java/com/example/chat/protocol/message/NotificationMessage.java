package com.example.chat.protocol.message;

import com.example.chat.protocol.ProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationMessage extends ProtocolMessage {
    private String title;       // 通知标题
    private String content;     // 通知内容
    private String level;       // 通知级别(info/warning/error)
    private long timestamp;     // 通知时间戳
    
    public NotificationMessage() {
        setType(MessageType.NOTIFICATION);
    }
}