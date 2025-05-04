package com.example.chat.service;

import com.example.chat.core.server.ChatServer;
import com.example.chat.model.ChatMessage;
import com.example.chat.model.Room;
import com.example.chat.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @Mock
    private RoomService roomService;
    
    @Mock
    private ChatServer chatServer;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 创建测试房间
        Room testRoom = new Room();
        testRoom.setId("room1");
        testRoom.setName("Test Room");
        
        when(roomService.getRoomById("room1")).thenReturn(testRoom);
        
        // 初始化消息服务 - 使用无参构造函数
        messageService = new MessageServiceImpl();
    }

    @Test
    void testSaveMessage() {
        // 创建测试消息
        ChatMessage message = ChatMessage.builder()
                .senderId("user1")
                .content("Hello, world!")
                .roomId("room1")
                .timestamp(System.currentTimeMillis())
                .type(ChatMessage.MessageType.TEXT)
                .build();
        
        // 保存消息
        messageService.saveMessage(message);
        ChatMessage savedMessage = message; // 或者从某处获取保存的消息
        
        // 验证消息ID已生成
        assertNotNull(savedMessage.getId());
        assertEquals("Hello, world!", savedMessage.getContent());
    }

    @Test
    void testGetRoomMessages() {
        // 创建并保存测试消息
        for (int i = 0; i < 5; i++) {
            ChatMessage message = ChatMessage.builder()
                    .senderId("user1")
                    .content("Message " + i)
                    .roomId("room1")
                    .timestamp(System.currentTimeMillis())
                    .type(ChatMessage.MessageType.TEXT)
                    .build();
            messageService.saveMessage(message);
        }
        
        // 获取房间消息
        List<ChatMessage> messages = messageService.getRoomMessages("room1", 10);
        
        // 验证消息数量
        assertEquals(5, messages.size());
    }

    @Test
    void testGetMessagesBetweenUsers() {
        // 创建并保存用户间的消息
        for (int i = 0; i < 3; i++) {
            ChatMessage message = ChatMessage.builder()
                    .senderId("user1")
                    .receiverId("user2")
                    .content("Message from user1 to user2: " + i)
                    .timestamp(System.currentTimeMillis())
                    .type(ChatMessage.MessageType.TEXT)
                    .build();
            messageService.saveMessage(message);
        }
        
        for (int i = 0; i < 2; i++) {
            ChatMessage message = ChatMessage.builder()
                    .senderId("user2")
                    .receiverId("user1")
                    .content("Message from user2 to user1: " + i)
                    .timestamp(System.currentTimeMillis())
                    .type(ChatMessage.MessageType.TEXT)
                    .build();
            messageService.saveMessage(message);
        }
        
        // 获取用户间的消息
        List<ChatMessage> messages = messageService.getMessagesBetweenUsers("user1", "user2");
        
        // 验证消息数量
        assertEquals(5, messages.size());
    }

    @Test
    void testSendMessage() {
        // 创建测试消息
        ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .senderId("user1")
                .receiverId("user2")
                .content("Hello, user2!")
                .timestamp(System.currentTimeMillis())
                .type(ChatMessage.MessageType.TEXT)
                .build();
        
        // 发送消息
        messageService.sendMessage(message);
        
        // 验证消息已发送给接收者
        verify(chatServer).sendMessageToUser("user2", message);
    }
}











