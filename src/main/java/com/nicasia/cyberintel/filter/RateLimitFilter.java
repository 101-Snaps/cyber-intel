package com.nicasia.cyberintel.filter;

import com.nicasia.cyberintel.service.RateLimitService;
import com.nicasia.cyberintel.service.SecurityMonitorService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final SecurityMonitorService securityMonitorService;

    public RateLimitFilter(RateLimitService rateLimitService,
                           SecurityMonitorService securityMonitorService) {
        this.rateLimitService = rateLimitService;
        this.securityMonitorService = securityMonitorService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // FIX Bug 1: Exempt auth endpoints from rate limiting so login/register
        // are never blocked by background dashboard polling.
        if (uri.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();

        if (!rateLimitService.isAllowed(ip)) {
            securityMonitorService.createAlert(
                "RATE_LIMIT_EXCEEDED",
                "MEDIUM",
                "unknown",
                ip,
                "Rate limit exceeded on: " + uri
            );
            response.setStatus(429);
            response.getWriter().write("Too many requests. Please slow down.");
            return;
        }

        chain.doFilter(request, response);
    }
}
