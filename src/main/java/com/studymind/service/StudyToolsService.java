package com.studymind.service;

import com.studymind.ai.AIService;
import com.studymind.model.ExtractedDocument;
import com.studymind.model.ExtractedPage;
import org.springframework.stereotype.Service;

/**
 * StudyToolsService coordinates active recall study aid generation:
 * Summaries, 5 MCQs, and 5 Flashcards.
 *
 * OOP / Viva Concepts:
 * - Single Responsibility Principle (SRP): Isolates generation of study aids
 *   from standard conversational chat.
 */
@Service
public class StudyToolsService {

    private final AIService aiService;
    private final DocumentIngestionService ingestionService;
    private final PromptService promptService;

    public StudyToolsService(AIService aiService,
                             DocumentIngestionService ingestionService,
                             PromptService promptService) {
        this.aiService = aiService;
        this.ingestionService = ingestionService;
        this.promptService = promptService;
    }

    public String generateSummary() {
        ExtractedDocument doc = getActiveDocOrThrow();
        String content = getSampledDocumentContent(doc);
        String prompt = promptService.buildSummaryPrompt(content, doc.getFileName());
        return aiService.generateResponse(prompt);
    }

    public String generateMcqs() {
        ExtractedDocument doc = getActiveDocOrThrow();
        String content = getSampledDocumentContent(doc);
        String prompt = promptService.buildMcqsPrompt(content, doc.getFileName());
        return aiService.generateResponse(prompt);
    }

    public String generateFlashcards() {
        ExtractedDocument doc = getActiveDocOrThrow();
        String content = getSampledDocumentContent(doc);
        String prompt = promptService.buildFlashcardsPrompt(content, doc.getFileName());
        return aiService.generateResponse(prompt);
    }

    private ExtractedDocument getActiveDocOrThrow() {
        ExtractedDocument doc = ingestionService.getCurrentDocument();
        if (doc == null) {
            throw new IllegalStateException("Please upload a study document (PDF, TXT, or Excel) first before generating study aids.");
        }
        return doc;
    }

    /**
     * Extracts text content up to a safe limit to avoid LLM context overflow.
     */
    private String getSampledDocumentContent(ExtractedDocument doc) {
        StringBuilder sb = new StringBuilder();
        int charLimit = 8000; // ~1500 - 2000 words safe window

        for (ExtractedPage page : doc.getPages()) {
            if (sb.length() >= charLimit) break;
            sb.append("\n[Page ").append(page.getPageNumber()).append("]\n");
            sb.append(page.getContent());
        }

        if (sb.length() > charLimit) {
            return sb.substring(0, charLimit) + "... [Content truncated for summary]";
        }
        return sb.toString().trim();
    }
}
