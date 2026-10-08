package com.studymind.dto;

/**
 * Data Transfer Object (DTO) for sending chat responses back to the client.
 */
public class ChatResponse {

    private boolean success;
    private String reply;
    private String error;
    private Long conversationId;

    public ChatResponse() {
    }

    public static ChatResponse success(String reply, Long conversationId) {
        ChatResponse response = new ChatResponse();
        response.success = true;
        response.reply = reply;
        response.conversationId = conversationId;
        return response;
    }

    public static ChatResponse failure(String error) {
        ChatResponse response = new ChatResponse();
        response.success = false;
        response.error = error;
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReply() {
        return reply;
    }

    public String getError() {
        return error;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
}
