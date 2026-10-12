package com.studymind.security;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.net.URI;
import java.util.Set;

/**
 * InputSanitizer provides defensive input sanitization to protect against:
 * 1. Cross-Site Scripting (XSS) via HTML stripping.
 * 2. Formula Injection (CSV / Excel Injection).
 * 3. Path Traversal attacks in filenames.
 * 4. Server-Side Request Forgery (SSRF) in external URL parameters.
 */
public final class InputSanitizer {

    private static final Set<String> ALLOWED_YOUTUBE_HOSTS = Set.of(
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com",
            "youtu.be"
    );

    private InputSanitizer() {
        // Utility class
    }

    /**
     * Sanitizes plain text input by stripping all HTML and JavaScript tags.
     */
    public static String sanitizeText(String input) {
        if (input == null) {
            return "";
        }
        return Jsoup.clean(input.trim(), Safelist.none());
    }

    /**
     * Protects Excel exports from CSV/Formula injection.
     * Prevents execution of formulas like =CMD|' /C calc'!A0
     */
    public static String sanitizeForExcel(String input) {
        if (input == null) {
            return "";
        }
        String cleaned = sanitizeText(input);
        if (cleaned.startsWith("=") || cleaned.startsWith("+") || cleaned.startsWith("-") || cleaned.startsWith("@")) {
            return "'" + cleaned;
        }
        return cleaned;
    }

    /**
     * Sanitizes uploaded filenames to prevent directory traversal attacks (e.g. ../../evil.pdf).
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unnamed_document";
        }
        // Extract base name and strip path separators
        String clean = filename.replace("\\", "/");
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash >= 0) {
            clean = clean.substring(lastSlash + 1);
        }
        // Remove null bytes and non-printable characters
        clean = clean.replaceAll("[\\p{Cntrl}]", "");
        // Remove directory traversal sequences
        clean = clean.replace("..", "").trim();
        return clean.isEmpty() ? "unnamed_document" : clean;
    }

    /**
     * Validates that a YouTube URL is strictly using http/https and points to authorized YouTube domains.
     * Prevents Server-Side Request Forgery (SSRF).
     */
    public static boolean isValidYouTubeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            host = host.toLowerCase();
            return ALLOWED_YOUTUBE_HOSTS.contains(host) || host.endsWith(".youtube.com");
        } catch (Exception e) {
            return false;
        }
    }
}
