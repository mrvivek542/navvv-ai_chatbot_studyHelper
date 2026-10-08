package com.studymind.model;

/**
 * ExtractedPage represents text extracted from a specific page of an uploaded file.
 *
 * OOP / Viva Concepts:
 * - Immutable Data Modeling: Encapsulates page number and page text.
 * - Source Grounding: Foundation for precise file and page citations in RAG.
 */
public class ExtractedPage {

    private final int pageNumber;
    private final String content;

    public ExtractedPage(int pageNumber, String content) {
        this.pageNumber = pageNumber;
        this.content = content != null ? content : "";
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getContent() {
        return content;
    }
}
