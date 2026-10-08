package com.studymind.controller;

import com.studymind.service.StudyToolsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API Controller for Active Recall Study Aids (Summary, MCQs, Flashcards).
 */
@RestController
@RequestMapping("/api/study")
public class StudyToolsApiController {

    private final StudyToolsService studyToolsService;

    public StudyToolsApiController(StudyToolsService studyToolsService) {
        this.studyToolsService = studyToolsService;
    }

    @PostMapping("/summary")
    public ResponseEntity<?> generateSummary() {
        try {
            String result = studyToolsService.generateSummary();
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Failed to generate summary."));
        }
    }

    @PostMapping("/mcqs")
    public ResponseEntity<?> generateMcqs() {
        try {
            String result = studyToolsService.generateMcqs();
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Failed to generate MCQs."));
        }
    }

    @PostMapping("/flashcards")
    public ResponseEntity<?> generateFlashcards() {
        try {
            String result = studyToolsService.generateFlashcards();
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Failed to generate flashcards."));
        }
    }
}
