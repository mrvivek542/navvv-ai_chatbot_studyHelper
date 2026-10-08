package com.studymind.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * GeminiAIService implements AIService using pure Java standard library (java.net.http.HttpClient).
 *
 * OOP / Viva Concept:
 * - Implementation of Interface (Polymorphism): implements AIService.
 * - Encapsulation: Network details, API endpoints, JSON serialization/deserialization
 *   are encapsulated within this class.
 * - Dependency Injection: API key is injected via @Value from environment variables,
 *   preventing credential exposure.
 */
@Service
public class GeminiAIService implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiAIService.class);

    private final HttpClient httpClient;

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String model;

    public GeminiAIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String generateResponse(String prompt) {
        return generateResponseWithModel(prompt, this.model);
    }

    @Override
    public String generateResponseWithModel(String prompt, String modelName) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Gemini API key is not configured.");
            return "Notice: Gemini API Key is not set. Please set the GEMINI_API_KEY environment variable to chat with the live AI model.";
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            return "Please enter a valid message.";
        }

        String chosenModel = (modelName != null && !modelName.trim().isEmpty()) ? modelName.trim() : this.model;

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" 
                    + chosenModel + ":generateContent?key=" + apiKey.trim();

            String escapedPrompt = escapeJson(prompt);
            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractTextFromResponse(response.body());
            } else {
                logger.error("Gemini API responded with status {}: {}", response.statusCode(), response.body());
                if (response.statusCode() == 400) {
                    return "AI Request Error: Invalid request or unsupported prompt structure.";
                } else if (response.statusCode() == 403 || response.statusCode() == 401) {
                    return "AI Authentication Error: The provided Gemini API Key is invalid or expired.";
                } else if (response.statusCode() == 429) {
                    return "AI Rate Limit: The free-tier quota is temporarily exhausted. Please retry in a few seconds.";
                }
                return "AI Service temporarily unavailable (Status code: " + response.statusCode() + ").";
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Network error communicating with Gemini API", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return "Network Error: Could not connect to AI service. Please check your internet connection.";
        } catch (Exception e) {
            logger.error("Unexpected error in GeminiAIService", e);
            return "Internal Error: Unable to process response right now.";
        }
    }

    /**
     * Extracts response text from Gemini's JSON without heavy external dependencies.
     */
    private String extractTextFromResponse(String json) {
        try {
            String marker = "\"text\": \"";
            int startIndex = json.indexOf(marker);
            if (startIndex == -1) {
                return "I received an answer, but could not parse the response text.";
            }

            startIndex += marker.length();
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;

            for (int i = startIndex; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaped) {
                    if (c == 'n') sb.append('\n');
                    else if (c == 'r') sb.append('\r');
                    else if (c == 't') sb.append('\t');
                    else if (c == '"') sb.append('"');
                    else if (c == '\\') sb.append('\\');
                    else sb.append(c);
                    escaped = false;
                } else {
                    if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        break; // End of JSON string
                    } else {
                        sb.append(c);
                    }
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            logger.warn("Failed to parse Gemini JSON output cleanly: {}", e.getMessage());
            return "Successfully received response, but encountered a formatting issue.";
        }
    }

    /**
     * Escapes standard special characters for JSON payloads.
     */
    private String escapeJson(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String hex = String.format("%04x", (int) c);
                        sb.append("\\u").append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
