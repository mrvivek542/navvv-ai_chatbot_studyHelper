package com.studymind.controller;

import com.studymind.dto.ChatRequest;
import com.studymind.dto.ChatResponse;
import com.studymind.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller handling AI Chat requests asynchronously via JSON.
 *
 * OOP / Viva Concept:
 * Thin Controller principle: Controller validates inputs and immediately
 * delegates processing to ChatService.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        if (request == null || request.getMessage() == null) {
            return ResponseEntity.badRequest().body(ChatResponse.failure("Message cannot be empty."));
        }

        String sanitizedMessage = com.studymind.security.InputSanitizer.sanitizeText(request.getMessage());
        if (sanitizedMessage.isEmpty()) {
            return ResponseEntity.badRequest().body(ChatResponse.failure("Message cannot be empty or contain only unsafe characters."));
        }

        try {
            ChatResponse response = chatService.processChatWithHistory(sanitizedMessage, request.getConversationId(), request.getModel());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.ok(ChatResponse.failure("An error occurred while communicating with the AI tutor."));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getAllConversations() {
        return ResponseEntity.ok(chatService.getAllConversations());
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> getConversationMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getConversationMessages(id));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<?> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return ResponseEntity.ok().build();
    }
}
