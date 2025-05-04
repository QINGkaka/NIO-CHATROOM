package com.example.chat.util;

import com.example.chat.protocol.MessageType;
import com.example.chat.protocol.response.SystemMessage;

/**
 * 系统消息工具类
 */
public class SystemMessageUtil {
    
    /**
     * 创建系统消息
     */
    public static SystemMessage createSystemMessage(String title, String content, String level) {
        SystemMessage message = new SystemMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setLevel(level);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    /**
     * 创建通知消息
     */
    public static SystemMessage createNotification(String title, String content) {
        SystemMessage message = new SystemMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setLevel("info");
        message.setTimestamp(System.currentTimeMillis());
        message.setType(MessageType.NOTIFICATION);
        return message;
    }
}




