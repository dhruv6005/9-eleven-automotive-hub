package com.nineeleven.automotive.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthRateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Set<String> trustedProxyAddresses;
    private final long requestLimit;
    private final Duration refillPeriod;

    public AuthRateLimitFilter(String trustedProxyAddresses) {
        this(parseTrustedProxyAddresses(trustedProxyAddresses), 30, Duration.ofMinutes(1));
    }

    AuthRateLimitFilter(Set<String> trustedProxyAddresses, long requestLimit, Duration refillPeriod) {
        this.trustedProxyAddresses = Set.copyOf(trustedProxyAddresses);
        this.requestLimit = requestLimit;
        this.refillPeriod = refillPeriod;
    }

    private static Set<String> parseTrustedProxyAddresses(String addresses) {
        if (addresses == null || addresses.isBlank()) return Set.of();
        return Arrays.stream(addresses.split(","))
                .map(String::trim)
                .filter(address -> !address.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/auth/");
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ip = clientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, ignored -> Bucket.builder()
                .addLimit(Bandwidth.classic(requestLimit, Refill.greedy(requestLimit, refillPeriod))).build());
        if (bucket.tryConsume(1)) { chain.doFilter(request, response); return; }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/problem+json");
        response.getWriter().write("{\"title\":\"Too many requests\",\"status\":429,\"detail\":\"Please wait before trying again.\"}");
    }

    private String clientIp(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        if (remoteAddress == null || remoteAddress.isBlank()) return "unknown";
        if (!trustedProxyAddresses.contains(remoteAddress)) return remoteAddress;

        // Nginx replaces this header with its socket peer address. Accept one literal only;
        // never interpret an arbitrary forwarding chain or hostname as a client IP.
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank() || forwardedFor.contains(",")) return remoteAddress;
        String candidate = forwardedFor.trim();
        return isIpLiteral(candidate) ? candidate : remoteAddress;
    }

    private boolean isIpLiteral(String value) {
        if (value.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) {
            for (String octet : value.split("\\.")) {
                if (Integer.parseInt(octet) > 255) return false;
            }
            return true;
        }
        return value.indexOf(':') >= 0 && value.matches("[0-9a-fA-F:]+");
    }
}
