package com.studymind.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RateLimitingFilter protects API endpoints against Denial of Service (DoS),
 * brute-force attempts, and Gemini AI quota exhaustion.
 *
 * Implements a memory-efficient sliding-window token counter per IP address.
 */
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_GENERAL_REQUESTS_PER_MINUTE = 120;
    private static final int MAX_AI_REQUESTS_PER_MINUTE = 30;

    private static class ClientBucket {
        long windowStart = System.currentTimeMillis();
        final AtomicInteger generalCount = new AtomicInteger(0);
        final AtomicInteger aiCount = new AtomicInteger(0);

        synchronized void resetIfExpired(long now) {
            if (now - windowStart > 60_000) {
                windowStart = now;
                generalCount.set(0);
                aiCount.set(0);
            }
        }
    }

    private final Map<String, ClientBucket> clientBuckets = new ConcurrentHashMap<>();
    private long lastCleanupTime = System.currentTimeMillis();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip static resources from rate-limiting
        if (isStaticAsset(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        long now = System.currentTimeMillis();

        periodicCleanup(now);

        ClientBucket bucket = clientBuckets.computeIfAbsent(clientIp, k -> new ClientBucket());
        bucket.resetIfExpired(now);

        // Check general request limit
        if (bucket.generalCount.incrementAndGet() > MAX_GENERAL_REQUESTS_PER_MINUTE) {
            sendRateLimitResponse(response, "Too many requests. Please slow down and try again shortly.");
            return;
        }

        // Check AI / tool intensive limit
        if (isAiIntensiveEndpoint(path)) {
            if (bucket.aiCount.incrementAndGet() > MAX_AI_REQUESTS_PER_MINUTE) {
                sendRateLimitResponse(response, "AI generation request limit reached. Please wait a minute before requesting more AI summaries or questions.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isStaticAsset(String path) {
        return path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || path.endsWith(".ico") || path.endsWith(".png") || path.endsWith(".svg");
    }

    private boolean isAiIntensiveEndpoint(String path) {
        return path.contains("/api/chat/send")
                || path.contains("/api/tools/")
                || path.contains("/api/modern/");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.trim().isEmpty()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");
        String json = "{\"success\":false,\"error\":\"" + message + "\",\"status\":429}";
        response.getWriter().write(json);
    }

    private synchronized void periodicCleanup(long now) {
        if (now - lastCleanupTime > 300_000) { // Every 5 minutes
            lastCleanupTime = now;
            clientBuckets.entrySet().removeIf(entry -> now - entry.getValue().windowStart > 120_000);
        }
    }
}
