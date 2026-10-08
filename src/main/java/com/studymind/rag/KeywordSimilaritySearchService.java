package com.studymind.rag;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * KeywordSimilaritySearchService provides in-memory text similarity matching
 * using TF-IDF and keyword frequency without heavy vector databases.
 *
 * OOP / Viva Concepts:
 * - Information Retrieval (IR) Fundamentals:
 *   Term Frequency (TF) = count(term in chunk) / total words in chunk.
 *   Inverse Document Frequency (IDF) = log(1 + total chunks / chunks containing term).
 * - Collections & Sorting: Uses Java Collections and custom Comparators to rank chunks.
 */
@Service
public class KeywordSimilaritySearchService {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
            "can", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
            "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him", "himself",
            "his", "how", "i", "if", "in", "into", "is", "isn't", "it", "its", "itself", "me", "more", "most",
            "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought",
            "our", "ours", "ourselves", "out", "over", "own", "same", "she", "should", "so", "some", "such",
            "than", "that", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they",
            "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why", "with", "would", "you", "your"
    );

    public static class ScoredChunk {
        private final TextChunk chunk;
        private final double score;

        public ScoredChunk(TextChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }

        public TextChunk getChunk() {
            return chunk;
        }

        public double getScore() {
            return score;
        }
    }

    /**
     * Finds top N most relevant chunks for a user's question.
     *
     * @param query The student's question.
     * @param allChunks The indexed document chunks.
     * @param topK Number of top matches to return (default 3).
     * @return List of ScoredChunk ordered from highest to lowest similarity.
     */
    public List<ScoredChunk> findTopChunks(String query, List<TextChunk> allChunks, int topK) {
        if (query == null || query.trim().isEmpty() || allChunks == null || allChunks.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> queryTerms = tokenizeAndFilter(query);
        if (queryTerms.isEmpty()) {
            return Collections.emptyList();
        }

        int totalDocs = allChunks.size();

        // 1. Calculate Document Frequency (DF) for each query term
        Map<String, Integer> docFrequency = new HashMap<>();
        for (String term : queryTerms) {
            int df = 0;
            for (TextChunk chunk : allChunks) {
                if (chunk.getContent().toLowerCase().contains(term)) {
                    df++;
                }
            }
            docFrequency.put(term, df);
        }

        // 2. Score each chunk using TF-IDF sum for query terms
        List<ScoredChunk> scoredList = new ArrayList<>();
        for (TextChunk chunk : allChunks) {
            String lowerContent = chunk.getContent().toLowerCase();
            String[] chunkWords = lowerContent.split("\\W+");
            int chunkLength = Math.max(chunkWords.length, 1);

            // Compute term frequencies in this chunk
            Map<String, Integer> termCounts = new HashMap<>();
            for (String w : chunkWords) {
                termCounts.put(w, termCounts.getOrDefault(w, 0) + 1);
            }

            double totalScore = 0.0;
            for (String term : queryTerms) {
                int count = termCounts.getOrDefault(term, 0);
                if (count > 0) {
                    double tf = (double) count / chunkLength;
                    int df = docFrequency.getOrDefault(term, 0);
                    // Standard smoothed IDF formula
                    double idf = Math.log(1.0 + ((double) totalDocs / (1.0 + df)));
                    totalScore += (tf * idf);
                }
            }

            // Only consider chunks with a non-zero matching score
            if (totalScore > 0.0) {
                scoredList.add(new ScoredChunk(chunk, totalScore));
            }
        }

        // 3. Sort by score in descending order (highest score first)
        scoredList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 4. Return topK matches
        return scoredList.size() > topK ? scoredList.subList(0, topK) : scoredList;
    }

    private List<String> tokenizeAndFilter(String text) {
        String[] tokens = text.toLowerCase().split("\\W+");
        List<String> valid = new ArrayList<>();
        for (String token : tokens) {
            String clean = token.trim();
            if (clean.length() >= 2 && !STOP_WORDS.contains(clean)) {
                valid.add(clean);
            }
        }
        return valid;
    }
}
