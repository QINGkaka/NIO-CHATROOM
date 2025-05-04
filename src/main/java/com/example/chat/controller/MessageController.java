package com.example.chat.controller;

import com.example.chat.model.ChatMessage;
import com.example.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class MessageController {
    
    private final MessageService messageService;
    
    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable String userId,
            @RequestAttribute("userId") String currentUserId) {
        
        List<ChatMessage> messages = messageService.getMessagesBetweenUsers(currentUserId, userId);
        return ResponseEntity.ok(messages);
    }
    
    @PostMapping("/{receiverId}")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable String receiverId,
            @RequestBody Map<String, Object> messageData,
            @RequestAttribute("userId") String userId) {
        
        String text = (String) messageData.get("text");
        String image = (String) messageData.get("image");
        
        ChatMessage message = ChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .senderId(userId)
                .content(text != null ? text : image)
                .receiverId(receiverId)
                .timestamp(System.currentTimeMillis())
                .type(image != null ? ChatMessage.MessageType.IMAGE : ChatMessage.MessageType.TEXT)
                .build();
        
        messageService.sendMessage(message);
        
        return ResponseEntity.ok(message);
    }
}



