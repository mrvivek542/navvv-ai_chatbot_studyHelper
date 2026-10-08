package com.studymind.rag;

/**
 * TextChunk represents a ~500 word segment of study material with precise source tracking.
 *
 * OOP / Viva Concepts:
 * - Immutable Data Carrier: Once created, chunk content and origin cannot be modified.
 * - Source Attribution: Tracks original fileName and pageNumber for RAG citations.
 */
public class TextChunk {

    private final String id;
    private final String fileName;
    private final int pageNumber;
    private final String content;
    private final int wordCount;

    public TextChunk(String id, String fileName, int pageNumber, String content) {
        this.id = id;
        this.fileName = fileName;
        this.pageNumber = pageNumber;
        this.content = content != null ? content.trim() : "";
        this.wordCount = this.content.isEmpty() ? 0 : this.content.split("\\s+").length;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getContent() {
        return content;
    }

    public int getWordCount() {
        return wordCount;
    }
}
