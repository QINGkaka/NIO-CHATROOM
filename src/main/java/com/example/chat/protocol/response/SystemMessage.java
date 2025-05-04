package com.example.chat.protocol.response;

import com.example.chat.protocol.AbstractProtocolMessage;
import com.example.chat.protocol.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 系统消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class SystemMessage extends AbstractProtocolMessage {
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息级别
     */
    private String level;
    
    /**
     * 默认构造函数
     */
    public SystemMessage() {
        super(MessageType.SYSTEM);
    }
    
    /**
     * 带参数构造函数
     */
    public SystemMessage(String title, String content, String level) {
        super(MessageType.SYSTEM);
        this.title = title;
        this.setContent(content);
        this.level = level;
    }
}



