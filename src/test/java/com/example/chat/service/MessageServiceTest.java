package com.example.chat.service;

import com.example.chat.dao.MessageDao;
import com.example.chat.dao.RoomDao;
import com.example.chat.model.ChatMessage;
import com.example.chat.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class MessageServiceTest {
    @Mock
    private MessageDao messageDao;
    
    @Mock
    private RoomDao roomDao;
    
    private MessageService messageService;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        messageService = new MessageServiceImpl(messageDao, roomDao);
    }
    
    @Test
    public void testSendMessage() {
        ChatMessage ProtocolMessage = ChatMessage.builder()
            .messageId("test-id")
            .content("test ProtocolMessage")
            .build();
            
        messageService.sendMessage(ProtocolMessage);
        verify(messageDao).save(ProtocolMessage);
    }
}


