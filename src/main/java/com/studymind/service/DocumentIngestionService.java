package com.studymind.service;

import com.studymind.model.ExtractedDocument;
import com.studymind.model.ExtractedPage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * DocumentIngestionService handles upload validation and text extraction
 * for PDF and TXT study files.
 *
 * OOP / Viva Concepts:
 * - Single Responsibility Principle (SRP):
 *   This service only validates and extracts text, keeping parsing separate from RAG.
 * - Resource Management:
 *   Try-with-resources handles closing PDDocument, preventing memory leaks.
 */
@Service
public class DocumentIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIngestionService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    // In-memory reference of currently active study document
    private volatile ExtractedDocument currentDocument;

    public ExtractedDocument getCurrentDocument() {
        return currentDocument;
    }

    public void clearCurrentDocument() {
        this.currentDocument = null;
    }

    public ExtractedDocument processUploadedFile(MultipartFile file) throws IllegalArgumentException, Exception {
        // 1. Validation: Null / Empty check
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a non-empty file to upload.");
        }

        // 2. Validation: File size (< 5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5 MB limit. Please upload a smaller study file.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "uploaded_file";
        }

        String lowerName = originalFilename.toLowerCase();
        ExtractedDocument doc;

        if (lowerName.endsWith(".pdf")) {
            doc = extractPdfText(file, originalFilename);
        } else if (lowerName.endsWith(".txt")) {
            doc = extractTxtText(file, originalFilename);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Only PDF and TXT study files are supported.");
        }

        if (doc.getTotalWords() == 0) {
            throw new IllegalArgumentException("The uploaded file does not contain readable text (e.g. scanned image-only PDF).");
        }

        this.currentDocument = doc;
        logger.info("Successfully extracted {} pages ({} words) from {}", doc.getTotalPages(), doc.getTotalWords(), doc.getFileName());
        return doc;
    }

    /**
     * Extracts text page-by-page using Apache PDFBox 3.x
     */
    private ExtractedDocument extractPdfText(MultipartFile file, String filename) throws Exception {
        ExtractedDocument doc = new ExtractedDocument(filename, "pdf", file.getSize());

        try (PDDocument pdfDocument = Loader.loadPDF(file.getBytes())) {
            int numberOfPages = pdfDocument.getNumberOfPages();
            PDFTextStripper textStripper = new PDFTextStripper();

            for (int page = 1; page <= numberOfPages; page++) {
                textStripper.setStartPage(page);
                textStripper.setEndPage(page);
                String pageText = textStripper.getText(pdfDocument).trim();
                doc.addPage(new ExtractedPage(page, pageText));
            }
        } catch (Exception e) {
            logger.error("Error reading PDF with Apache PDFBox", e);
            throw new IllegalArgumentException("Could not parse the PDF file. The file may be corrupted or password-protected.");
        }

        return doc;
    }

    /**
     * Extracts text from plain text files with UTF-8 encoding
     */
    private ExtractedDocument extractTxtText(MultipartFile file, String filename) throws Exception {
        ExtractedDocument doc = new ExtractedDocument(filename, "txt", file.getSize());

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        // TXT files have 1 single logical page
        doc.addPage(new ExtractedPage(1, content.toString().trim()));
        return doc;
    }
}
