package com.studymind.controller;

import com.studymind.service.DocumentExportService;
import com.studymind.service.YouTubeSummaryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ModernFeaturesController handles modern chatbot operations:
 * 1. YouTube video URL lecture summarization & notes.
 * 2. Exporting study notes into downloadable PDF files.
 * 3. Exporting study notes into downloadable Excel (.xlsx) workbooks.
 */
@RestController
@RequestMapping("/api/modern")
public class ModernFeaturesController {

    private final YouTubeSummaryService youTubeSummaryService;
    private final DocumentExportService exportService;

    public ModernFeaturesController(YouTubeSummaryService youTubeSummaryService,
                                    DocumentExportService exportService) {
        this.youTubeSummaryService = youTubeSummaryService;
        this.exportService = exportService;
    }

    @PostMapping("/youtube-summary")
    public ResponseEntity<?> summarizeYouTube(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.trim().isEmpty() || !com.studymind.security.InputSanitizer.isValidYouTubeUrl(url)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Please provide a valid, safe YouTube link (https://www.youtube.com/... or https://youtu.be/...)."));
        }

        try {
            String summary = youTubeSummaryService.summarizeYouTubeVideo(url.trim());
            return ResponseEntity.ok(Map.of("success", true, "data", summary));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Failed to summarize video: " + ex.getMessage()));
        }
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<?> exportNotesPdf(@RequestBody Map<String, String> request) {
        String rawTitle = request.getOrDefault("title", "StudyMind AI - Notes");
        String rawContent = request.getOrDefault("content", "No content provided.");

        String title = com.studymind.security.InputSanitizer.sanitizeText(rawTitle);
        String content = com.studymind.security.InputSanitizer.sanitizeText(rawContent);

        try {
            byte[] pdfBytes = exportService.generateNotesPdf(title, content);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"StudyMind_Notes.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Failed to generate PDF notes."));
        }
    }

    @PostMapping("/export/excel")
    public ResponseEntity<?> exportNotesExcel(@RequestBody Map<String, String> request) {
        String rawTitle = request.getOrDefault("title", "Study Topics");
        String rawContent = request.getOrDefault("content", "No content provided.");

        String title = com.studymind.security.InputSanitizer.sanitizeForExcel(rawTitle);
        String content = com.studymind.security.InputSanitizer.sanitizeForExcel(rawContent);

        try {
            byte[] excelBytes = exportService.generateStudyExcel(title, content);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"StudyMind_Sheet.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Failed to generate Excel sheet."));
        }
    }
}
