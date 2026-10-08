package com.studymind.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ExtractedDocument represents a parsed study document containing one or more pages.
 */
public class ExtractedDocument {

    private final String fileName;
    private final String fileType; // "pdf" or "txt"
    private final long fileSizeBytes;
    private final List<ExtractedPage> pages;

    public ExtractedDocument(String fileName, String fileType, long fileSizeBytes) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.pages = new ArrayList<>();
    }

    public void addPage(ExtractedPage page) {
        this.pages.add(page);
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public List<ExtractedPage> getPages() {
        return pages;
    }

    public int getTotalPages() {
        return pages.size();
    }

    public int getTotalWords() {
        int count = 0;
        for (ExtractedPage page : pages) {
            String text = page.getContent().trim();
            if (!text.isEmpty()) {
                count += text.split("\\s+").length;
            }
        }
        return count;
    }
}
