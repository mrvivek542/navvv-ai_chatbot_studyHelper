package com.studymind.dto;

/**
 * Data Transfer Object (DTO) for handling chat requests.
 *
 * OOP / Viva Concept:
 * Encapsulation: Grouping request fields into an immutable/structured object
 * rather than passing multiple loose method parameters.
 */
public class ChatRequest {

    private String message;
    private Long conversationId;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, Long conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
