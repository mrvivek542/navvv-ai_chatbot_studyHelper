package com.studymind.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymind.ai.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YouTubeSummaryService extracts video title, author, and syllabus chapters/timestamps
 * from YouTube videos and generates structured, comprehensive academic revision notes.
 */
@Service
public class YouTubeSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeSummaryService.class);
    private final AIService aiService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YouTubeSummaryService(AIService aiService, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public String extractVideoId(String url) {
        if (url == null) return null;
        String pattern = "(?:https?:\\/\\/)?(?:www\\.)?(?:youtube\\.com\\/(?:watch\\?v=|shorts\\/|embed\\/)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public String summarizeYouTubeVideo(String videoUrl) {
        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL. Please provide a valid YouTube watch link or youtu.be link.");
        }

        String title = "YouTube Video (" + videoId + ")";
        String author = "Educator / Creator";
        String videoDescription = "";

        try {
            // 1. Fetch clean video metadata via noembed
            String noembedUrl = "https://noembed.com/embed?url=https://www.youtube.com/watch?v=" + videoId;
            HttpRequest infoReq = HttpRequest.newBuilder()
                    .uri(URI.create(noembedUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> infoResp = httpClient.send(infoReq, HttpResponse.BodyHandlers.ofString());
            if (infoResp.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(infoResp.body());
                if (root.hasNonNull("title")) {
                    title = root.get("title").asText();
                }
                if (root.hasNonNull("author_name")) {
                    author = root.get("author_name").asText();
                }
            }

            // 2. Fetch HTML to extract chapter syllabus, description, and table of contents
            HttpRequest pageReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.youtube.com/watch?v=" + videoId))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(Duration.ofSeconds(12))
                    .GET()
                    .build();

            HttpResponse<String> pageResp = httpClient.send(pageReq, HttpResponse.BodyHandlers.ofString());
            if (pageResp.statusCode() == 200) {
                String html = pageResp.body();

                // If noembed failed to extract title, extract from HTML <title>
                if (title.startsWith("YouTube Video")) {
                    Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
                    Matcher titleMatcher = titlePattern.matcher(html);
                    if (titleMatcher.find()) {
                        String pageTitle = titleMatcher.group(1).replace(" - YouTube", "").trim();
                        if (!pageTitle.isEmpty()) {
                            title = pageTitle;
                        }
                    }
                }

                // Check shortDescription first
                Pattern descPattern = Pattern.compile("\"shortDescription\"\\s*:\\s*\"(.*?)\"");
                Matcher descMatcher = descPattern.matcher(html);
                if (descMatcher.find()) {
                    videoDescription = descMatcher.group(1)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                            .replace("\\r", "");
                } else {
                    Pattern altPattern = Pattern.compile("description\"\\s*:\\s*\\{\\s*\"simpleText\"\\s*:\\s*\"(.*?)\"\\}");
                    Matcher altMatcher = altPattern.matcher(html);
                    if (altMatcher.find()) {
                        videoDescription = altMatcher.group(1)
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\")
                                .replace("\\r", "");
                    }
                }
            }

        } catch (Exception e) {
            logger.warn("Could not fetch extended YouTube page metadata: {}", e.getMessage());
        }

        // 3. Build comprehensive academic lecture notes prompt
        StringBuilder sb = new StringBuilder();
        sb.append("You are StudyMind AI, an elite university professor and academic coach.\n");
        sb.append("Analyze this video lecture thoroughly and generate complete, detailed academic study notes:\n");
        sb.append("• Video Title: ").append(title).append("\n");
        sb.append("• Instructor / Channel: ").append(author).append("\n");
        sb.append("• Video URL: https://www.youtube.com/watch?v=").append(videoId).append("\n\n");

        if (!videoDescription.isEmpty()) {
            String sampleDesc = videoDescription.length() > 3000 ? videoDescription.substring(0, 3000) + "..." : videoDescription;
            sb.append("=== LECTURE CHAPTERS, SYLLABUS & TOPICS FROM VIDEO ===\n");
            sb.append(sampleDesc).append("\n");
            sb.append("=====================================================\n\n");
        }

        sb.append("INSTRUCTIONS FOR PRODUCING ACADEMIC LECTURE NOTES:\n");
        sb.append("1. **Executive Course Overview & Syllabus Breakdown**: Detail what the course covers based directly on the title ('").append(title).append("') and syllabus above.\n");
        sb.append("2. **Detailed Topic Notes & Core Concepts**: Deep-dive into the specific concepts taught in this lecture (e.g. key theory, rules, syntax/code examples if technical).\n");
        sb.append("3. **Essential Definitions & Formulae / Cheat Sheet**: High-yield terms every student must memorize for exams.\n");
        sb.append("4. **3 Exam-Style Practice Questions & Model Solutions**: Realistic semester exam questions assessing mastery of these exact topics.\n\n");
        sb.append("Do NOT give generic placeholders. Deliver comprehensive, rich study notes specifically tailored to '").append(title).append("'.\n\n");
        sb.append("PRODUCE LECTURE NOTES NOW:");

        return aiService.generateResponse(sb.toString());
    }
}
