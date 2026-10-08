package com.studymind.service;

import com.studymind.rag.KeywordSimilaritySearchService.ScoredChunk;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PromptService centralizes all prompt templates and formatting logic.
 *
 * OOP / Viva Concepts:
 * - Single Responsibility Principle (SRP): Isolates prompt engineering from orchestration logic.
 * - Hallucination Guardrail: Strictly instructs the LLM to only answer from provided context chunks
 *   and emit the mandatory fallback phrase if information is insufficient.
 */
@Service
public class PromptService {

    public static final String FALLBACK_NOT_FOUND_MESSAGE =
            "I couldn't find enough information in your uploaded sources.";

    /**
     * Builds a strict context-grounded RAG prompt with page and file citations.
     */
    public String buildGroundedRagPrompt(String userQuestion, List<ScoredChunk> relevantChunks) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are StudyMind AI, a strict, context-grounded academic study assistant.\n");
        sb.append("A student has asked a question based on their uploaded study materials.\n\n");
        sb.append("CRITICAL INSTRUCTIONS:\n");
        sb.append("1. Answer the question using ONLY the provided Source Context Chunks below.\n");
        sb.append("2. Do NOT use any external or pre-trained knowledge beyond the context.\n");
        sb.append("3. At the end of your answer, explicitly cite the source in this exact format: [Source: <FileName>, Page <PageNumber>].\n");
        sb.append("4. IF THE ANSWER IS NOT FOUND in the provided Source Context Chunks, you MUST respond with EXACTLY this sentence and nothing else:\n");
        sb.append("   \"").append(FALLBACK_NOT_FOUND_MESSAGE).append("\"\n\n");

        sb.append("=== PROVIDED SOURCE CONTEXT CHUNKS ===\n");
        for (int i = 0; i < relevantChunks.size(); i++) {
            ScoredChunk sc = relevantChunks.get(i);
            sb.append("--- CHUNK ").append(i + 1)
              .append(" [File: ").append(sc.getChunk().getFileName())
              .append(", Page: ").append(sc.getChunk().getPageNumber()).append("] ---\n");
            sb.append(sc.getChunk().getContent()).append("\n\n");
        }
        sb.append("======================================\n\n");

        sb.append("STUDENT QUESTION: ").append(userQuestion.trim()).append("\n\n");
        sb.append("YOUR GROUNDED ANSWER:");

        return sb.toString();
    }

    /**
     * Builds a prompt for generating a concise academic summary.
     */
    public String buildSummaryPrompt(String documentContent, String fileName) {
        return "You are StudyMind AI academic assistant. Generate a high-yield, structured summary of the following study material from '" 
                + fileName + "'.\n\n"
                + "Include:\n"
                + "1. Core Overview (2-3 sentences)\n"
                + "2. Key Concepts & Definitions (bullet points)\n"
                + "3. Important Takeaways for Exams\n\n"
                + "DOCUMENT CONTENT:\n" + documentContent;
    }

    /**
     * Builds a prompt for generating 5 Multiple Choice Questions with answers and explanations.
     */
    public String buildMcqsPrompt(String documentContent, String fileName) {
        return "You are StudyMind AI academic examiner. Based strictly on the following study material from '" 
                + fileName + "', generate EXACTLY 5 high-quality Multiple Choice Questions (MCQs).\n\n"
                + "Format each question clearly as follows:\n"
                + "Q[Number]: [Question statement]\n"
                + "A) [Option A]\n"
                + "B) [Option B]\n"
                + "C) [Option C]\n"
                + "D) [Option D]\n"
                + "Correct Answer: [Option letter]\n"
                + "Explanation: [Brief 1-sentence reasoning based on the text]\n\n"
                + "DOCUMENT CONTENT:\n" + documentContent;
    }

    /**
     * Builds a prompt for generating 5 Active Recall Flashcards (Question + Answer).
     */
    public String buildFlashcardsPrompt(String documentContent, String fileName) {
        return "You are StudyMind AI study coach. Based strictly on the following study material from '" 
                + fileName + "', generate EXACTLY 5 Active Recall Flashcards for quick student revision.\n\n"
                + "Format each flashcard clearly as:\n"
                + "CARD [Number]:\n"
                + "FRONT (Prompt / Concept / Question): [Brief concept to test]\n"
                + "BACK (Answer / Explanation): [Clear, concise definition or answer]\n\n"
                + "DOCUMENT CONTENT:\n" + documentContent;
    }
}
