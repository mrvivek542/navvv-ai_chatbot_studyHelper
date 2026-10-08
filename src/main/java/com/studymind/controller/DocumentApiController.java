package com.studymind.controller;

import com.studymind.dto.DocumentUploadResponse;
import com.studymind.model.ExtractedDocument;
import com.studymind.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST API Controller handling document uploads and file status.
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentApiController {

    private final DocumentIngestionService ingestionService;

    public DocumentApiController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            ExtractedDocument doc = ingestionService.processUploadedFile(file);
            return ResponseEntity.ok(DocumentUploadResponse.success(
                    "Study file uploaded and processed successfully!",
                    doc.getFileName(),
                    doc.getTotalPages(),
                    doc.getTotalWords()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(DocumentUploadResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(DocumentUploadResponse.failure(
                    "An error occurred while parsing the document: " + ex.getMessage()));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentDocument() {
        ExtractedDocument doc = ingestionService.getCurrentDocument();
        if (doc == null) {
            return ResponseEntity.ok().body(new Object() {
                public final boolean active = false;
            });
        }
        return ResponseEntity.ok(new Object() {
            public final boolean active = true;
            public final String fileName = doc.getFileName();
            public final String fileType = doc.getFileType();
            public final int totalPages = doc.getTotalPages();
            public final int totalWords = doc.getTotalWords();
        });
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearDocument() {
        ingestionService.clearCurrentDocument();
        return ResponseEntity.ok().build();
    }
}
