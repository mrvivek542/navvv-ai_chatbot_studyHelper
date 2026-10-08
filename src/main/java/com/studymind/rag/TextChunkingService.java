package com.studymind.rag;

import com.studymind.model.ExtractedDocument;
import com.studymind.model.ExtractedPage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * TextChunkingService splits large academic texts into manageable ~500 word blocks.
 *
 * OOP / Viva Concepts:
 * - Algorithmic Design: Preserves paragraph and sentence boundaries rather than cutting words in half.
 * - Single Responsibility Principle (SRP): Focuses exclusively on chunk generation.
 */
@Service
public class TextChunkingService {

    public static final int TARGET_CHUNK_WORDS = 450;
    public static final int OVERLAP_WORDS = 50;

    public List<TextChunk> chunkDocument(ExtractedDocument document) {
        List<TextChunk> chunks = new ArrayList<>();
        if (document == null || document.getPages().isEmpty()) {
            return chunks;
        }

        int chunkCounter = 1;

        for (ExtractedPage page : document.getPages()) {
            String pageText = page.getContent().trim();
            if (pageText.isEmpty()) continue;

            String[] words = pageText.split("\\s+");

            if (words.length <= TARGET_CHUNK_WORDS) {
                // Whole page fits in one chunk
                String chunkId = document.getFileName() + "_p" + page.getPageNumber() + "_c" + (chunkCounter++);
                chunks.add(new TextChunk(chunkId, document.getFileName(), page.getPageNumber(), pageText));
            } else {
                // Split multi-word page with sliding window overlap
                int start = 0;
                while (start < words.length) {
                    int end = Math.min(start + TARGET_CHUNK_WORDS, words.length);
                    StringBuilder sb = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        sb.append(words[i]).append(" ");
                    }

                    String chunkId = document.getFileName() + "_p" + page.getPageNumber() + "_c" + (chunkCounter++);
                    chunks.add(new TextChunk(chunkId, document.getFileName(), page.getPageNumber(), sb.toString().trim()));

                    if (end >= words.length) break;
                    // Advance with overlap
                    start += (TARGET_CHUNK_WORDS - OVERLAP_WORDS);
                }
            }
        }

        return chunks;
    }
}
