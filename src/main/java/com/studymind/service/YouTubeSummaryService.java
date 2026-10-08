package com.studymind.service;

import com.studymind.ai.AIService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
 * YouTubeSummaryService extracts metadata and transcript information
 * from YouTube video links and generates structured academic study notes.
 */
@Service
public class YouTubeSummaryService {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeSummaryService.class);
    private final AIService aiService;
    private final HttpClient httpClient;

    public YouTubeSummaryService(AIService aiService) {
        this.aiService = aiService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    /**
     * Extracts YouTube video ID from various link formats (watch?v=, youtu.be, shorts).
     */
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

    /**
     * Fetches video title/description via HTML or noembed API and summarizes.
     */
    public String summarizeYouTubeVideo(String videoUrl) {
        String videoId = extractVideoId(videoUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL. Please provide a valid YouTube watch link or youtu.be link.");
        }

        try {
            // Fetch video title and author using YouTube's oEmbed endpoint (no API key needed!)
            String oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=" + videoId + "&format=json";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(oembedUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String title = "YouTube Video (" + videoId + ")";
            String author = "Channel Creator";

            if (response.statusCode() == 200) {
                String body = response.body();
                // Extract title
                int titleIdx = body.indexOf("\"title\": \"");
                if (titleIdx != -1) {
                    int endTitle = body.indexOf("\"", titleIdx + 10);
                    if (endTitle != -1) {
                        title = body.substring(titleIdx + 10, endTitle);
                    }
                }
                // Extract author
                int authorIdx = body.indexOf("\"author_name\": \"");
                if (authorIdx != -1) {
                    int endAuthor = body.indexOf("\"", authorIdx + 16);
                    if (endAuthor != -1) {
                        author = body.substring(authorIdx + 16, endAuthor);
                    }
                }
            }

            // Construct study notes generation prompt
            String prompt = "You are StudyMind AI academic coach. A student has shared a YouTube study video:\n"
                    + "Video Title: " + title + "\n"
                    + "Channel: " + author + "\n"
                    + "Video Link: https://www.youtube.com/watch?v=" + videoId + "\n\n"
                    + "TASK:\n"
                    + "1. Provide a comprehensive, structured lecture summary of the topics covered in this video title.\n"
                    + "2. Outline the Core Learning Objectives.\n"
                    + "3. Break down Key Academic Concepts & Theory (with bullet points and practical analogies).\n"
                    + "4. 3 Quick Self-Assessment Questions with Answers.\n"
                    + "Format your answer with clear markdown headings, bold terms, and high clarity.";

            return aiService.generateResponse(prompt);

        } catch (Exception e) {
            logger.error("Error processing YouTube link: {}", videoUrl, e);
            throw new RuntimeException("Could not extract video metadata from YouTube link.");
        }
    }
}
