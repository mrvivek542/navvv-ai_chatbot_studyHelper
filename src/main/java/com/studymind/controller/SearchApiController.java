package com.studymind.controller;

import com.studymind.model.ExtractedDocument;
import com.studymind.rag.KeywordSimilaritySearchService;
import com.studymind.rag.KeywordSimilaritySearchService.ScoredChunk;
import com.studymind.rag.TextChunk;
import com.studymind.rag.TextChunkingService;
import com.studymind.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller demonstrating chunking and similarity search results.
 */
@RestController
@RequestMapping("/api/search")
public class SearchApiController {

    private final DocumentIngestionService ingestionService;
    private final TextChunkingService chunkingService;
    private final KeywordSimilaritySearchService searchService;

    public SearchApiController(DocumentIngestionService ingestionService,
                               TextChunkingService chunkingService,
                               KeywordSimilaritySearchService searchService) {
        this.ingestionService = ingestionService;
        this.chunkingService = chunkingService;
        this.searchService = searchService;
    }

    @GetMapping("/chunks")
    public ResponseEntity<?> getDocumentChunks() {
        ExtractedDocument doc = ingestionService.getCurrentDocument();
        if (doc == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active study document uploaded."));
        }
        List<TextChunk> chunks = chunkingService.chunkDocument(doc);
        return ResponseEntity.ok(chunks);
    }

    @PostMapping("/test")
    public ResponseEntity<?> testSimilaritySearch(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query cannot be empty."));
        }

        ExtractedDocument doc = ingestionService.getCurrentDocument();
        if (doc == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active document uploaded. Please upload a study file first."));
        }

        List<TextChunk> chunks = chunkingService.chunkDocument(doc);
        List<ScoredChunk> results = searchService.findTopChunks(query, chunks, 3);

        List<Map<String, Object>> response = new ArrayList<>();
        for (ScoredChunk item : results) {
            response.add(Map.of(
                    "chunkId", item.getChunk().getId(),
                    "pageNumber", item.getChunk().getPageNumber(),
                    "fileName", item.getChunk().getFileName(),
                    "score", Math.round(item.getScore() * 1000.0) / 1000.0,
                    "content", item.getChunk().getContent()
            ));
        }

        return ResponseEntity.ok(response);
    }
}
