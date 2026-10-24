package com.nineeleven.automotive.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuthRateLimitFilterTest {
    @Test
    void usesForwardedClientIpOnlyWhenRequestComesFromTrustedProxy() throws Exception {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(Set.of("172.30.0.10"), 1, Duration.ofHours(1));

        assertEquals(200, send(filter, "172.30.0.10", "198.51.100.21"));
        // Same forwarded client exhausts its bucket even though it arrived on a new request.
        assertEquals(429, send(filter, "172.30.0.10", "198.51.100.21"));

        // A direct caller spoofing that same XFF value is keyed by its socket peer instead.
        assertEquals(200, send(filter, "203.0.113.18", "198.51.100.21"));
    }

    private int send(AuthRateLimitFilter filter, String remoteAddress, String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr(remoteAddress);
        request.addHeader("X-Forwarded-For", forwardedFor);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }
}
