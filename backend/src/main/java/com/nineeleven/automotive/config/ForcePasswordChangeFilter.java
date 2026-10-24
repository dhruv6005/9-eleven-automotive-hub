package com.nineeleven.automotive.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

public class ForcePasswordChangeFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt jwt
                && Boolean.TRUE.equals(jwt.getClaim("forcePasswordChange"))
                && !isAllowedDuringForcedChange(request.getRequestURI())) {
            response.setStatus(403); response.setContentType("application/problem+json");
            response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Password change required\",\"status\":403,\"detail\":\"Change your initial password before continuing.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
    private boolean isAllowedDuringForcedChange(String path) {
        return path.equals("/api/v1/auth/change-password") || path.equals("/api/v1/auth/logout")
                || path.equals("/api/v1/auth/refresh");
    }
}
