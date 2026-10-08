package com.studymind.dto;

/**
 * Data Transfer Object for file upload responses.
 */
public class DocumentUploadResponse {

    private boolean success;
    private String message;
    private String fileName;
    private int totalPages;
    private int totalWords;

    public DocumentUploadResponse() {
    }

    public static DocumentUploadResponse success(String message, String fileName, int totalPages, int totalWords) {
        DocumentUploadResponse resp = new DocumentUploadResponse();
        resp.success = true;
        resp.message = message;
        resp.fileName = fileName;
        resp.totalPages = totalPages;
        resp.totalWords = totalWords;
        return resp;
    }

    public static DocumentUploadResponse failure(String message) {
        DocumentUploadResponse resp = new DocumentUploadResponse();
        resp.success = false;
        resp.message = message;
        return resp;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getFileName() {
        return fileName;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalWords() {
        return totalWords;
    }
}
