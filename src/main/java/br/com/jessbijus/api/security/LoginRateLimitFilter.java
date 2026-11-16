package br.com.jessbijus.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.filter.OncePerRequestFilter;

final class LoginRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MS = 900_000;
    private final Map<String, Bucket> buckets = new HashMap<>();
    private final Clock clock;
    private long nextCleanup;

    LoginRateLimitFilter() { this(Clock.systemUTC()); }
    LoginRateLimitFilter(Clock clock) { this.clock = clock; }

    private static final class Bucket {
        int failures;
        int pending;
        long expires;
    }

    private synchronized Bucket reserve(String key, int limit, HttpServletResponse response) {
        long now = clock.millis();
        if (now >= nextCleanup) {
            buckets.values().removeIf(bucket -> bucket.pending == 0 && bucket.expires <= now);
            nextCleanup = now + 1000;
        }
        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            if (buckets.size() >= 10_000) {
                block(response, WINDOW_MS);
                return null;
            }
            bucket = new Bucket();
            buckets.put(key, bucket);
        }
        if (bucket.expires <= now) bucket.failures = 0;
        if (bucket.failures + bucket.pending >= limit) {
            block(response, bucket.failures >= limit ? bucket.expires - now : 1000);
            return null;
        }
        bucket.pending++;
        return bucket;
    }

    private synchronized void finish(Bucket bucket, boolean failed, boolean reset, int limit, HttpServletResponse response) {
        bucket.pending--;
        long now = clock.millis();
        if (reset || bucket.expires <= now) bucket.failures = 0;
        if (failed) {
            if (bucket.failures == 0) bucket.expires = now + WINDOW_MS;
            if (++bucket.failures == limit) {
                bucket.expires = now + WINDOW_MS;
                block(response, WINDOW_MS);
            }
        }
    }

    private static void block(HttpServletResponse response, long milliseconds) {
        response.setStatus(429);
        response.setHeader("Retry-After", Long.toString(Math.max(1, (milliseconds + 999) / 1000)));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean login = path.equals("/api/auth/login");
        if (!"POST".equals(request.getMethod()) || (!login && !path.equals("/api/auth/password"))) {
            chain.doFilter(request, response);
            return;
        }
        int ipLimit = login ? 12 : 3;
        Bucket ip = reserve(path + ":" + request.getRemoteAddr(), ipLimit, response);
        if (ip == null) return;
        Bucket account = null;
        boolean failed = false;
        boolean success = false;
        try {
            if (login) {
                String username = request.getParameter("username");
                String key = username == null ? "" : username.trim();
                account = reserve("account:" + (key.length() <= 80 ? key : "*"), 3, response);
                if (account == null) return;
            }
            chain.doFilter(request, response);
            success = response.getStatus() == 204;
            failed = login ? response.getStatus() == 401 : response.getStatus() == 400;
        } finally {
            if (account != null) finish(account, failed, success, 3, response);
            finish(ip, failed, false, ipLimit, response);
        }
    }
}
