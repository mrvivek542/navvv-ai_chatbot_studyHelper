package com.studymind.service;

import com.studymind.ai.AIService;
import com.studymind.dto.ChatResponse;
import com.studymind.model.ChatMessage;
import com.studymind.model.Conversation;
import com.studymind.model.ExtractedDocument;
import com.studymind.rag.KeywordSimilaritySearchService;
import com.studymind.rag.KeywordSimilaritySearchService.ScoredChunk;
import com.studymind.rag.TextChunk;
import com.studymind.rag.TextChunkingService;
import com.studymind.repository.ChatMessageRepository;
import com.studymind.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ChatService coordinates conversation persistence, RAG retrieval orchestration,
 * and AI communication.
 *
 * OOP / Viva Concepts:
 * - Orchestrator Pattern: Directs queries to either standard AI chat or grounded RAG pipeline
 *   depending on whether an active document is loaded.
 * - Source Grounding & Anti-Hallucination: If relevant chunks score zero or lack sufficient info,
 *   politely communicates the absence of data.
 */
@Service
public class ChatService {

    private final AIService aiService;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DocumentIngestionService ingestionService;
    private final TextChunkingService chunkingService;
    private final KeywordSimilaritySearchService searchService;
    private final PromptService promptService;

    public ChatService(AIService aiService,
                       ConversationRepository conversationRepository,
                       ChatMessageRepository chatMessageRepository,
                       DocumentIngestionService ingestionService,
                       TextChunkingService chunkingService,
                       KeywordSimilaritySearchService searchService,
                       PromptService promptService) {
        this.aiService = aiService;
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.ingestionService = ingestionService;
        this.chunkingService = chunkingService;
        this.searchService = searchService;
        this.promptService = promptService;
    }

    @Transactional
    public Conversation createNewConversation(String title) {
        if (title == null || title.trim().isEmpty()) {
            title = "New Study Session";
        }
        Conversation conversation = new Conversation(title);
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getAllConversations() {
        return conversationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getConversationMessages(Long conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    @Transactional
    public ChatResponse processChatWithHistory(String userMessage, Long conversationId) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ChatResponse.failure("Message cannot be empty.");
        }

        Conversation conversation;
        if (conversationId == null) {
            String derivedTitle = userMessage.trim();
            if (derivedTitle.length() > 30) {
                derivedTitle = derivedTitle.substring(0, 30) + "...";
            }
            conversation = createNewConversation(derivedTitle);
        } else {
            conversation = conversationRepository.findById(conversationId)
                    .orElseGet(() -> createNewConversation("Study Session"));
        }

        // 1. Record User Message
        ChatMessage userChatMessage = new ChatMessage("user", userMessage.trim());
        conversation.addMessage(userChatMessage);
        chatMessageRepository.save(userChatMessage);

        String aiReply;
        ExtractedDocument activeDoc = ingestionService.getCurrentDocument();

        if (activeDoc != null) {
            // ==========================================
            // RAG PIPELINE EXECUTION
            // ==========================================
            List<TextChunk> docChunks = chunkingService.chunkDocument(activeDoc);
            List<ScoredChunk> topChunks = searchService.findTopChunks(userMessage, docChunks, 3);

            if (topChunks.isEmpty()) {
                // No keywords matched from the uploaded document
                aiReply = PromptService.FALLBACK_NOT_FOUND_MESSAGE;
            } else {
                // Send grounded context to AI
                String ragPrompt = promptService.buildGroundedRagPrompt(userMessage, topChunks);
                aiReply = aiService.generateResponse(ragPrompt);
            }
        } else {
            // ==========================================
            // STANDARD MULTI-TURN AI TUTOR PIPELINE
            // ==========================================
            List<ChatMessage> history = chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversation.getId());
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("You are StudyMind AI, a helpful, patient academic tutor. ")
                         .append("Below is the ongoing study dialogue history. Answer the student's latest question accurately:\n\n");

            for (ChatMessage msg : history) {
                if ("user".equalsIgnoreCase(msg.getSender())) {
                    promptBuilder.append("Student: ").append(msg.getContent()).append("\n");
                } else {
                    promptBuilder.append("Tutor: ").append(msg.getContent()).append("\n");
                }
            }
            promptBuilder.append("Tutor: ");
            aiReply = aiService.generateResponse(promptBuilder.toString());
        }

        // 2. Save and Return AI Response
        ChatMessage aiChatMessage = new ChatMessage("ai", aiReply);
        conversation.addMessage(aiChatMessage);
        chatMessageRepository.save(aiChatMessage);

        return ChatResponse.success(aiReply, conversation.getId());
    }

    @Transactional
    public void deleteConversation(Long conversationId) {
        conversationRepository.deleteById(conversationId);
    }
}
