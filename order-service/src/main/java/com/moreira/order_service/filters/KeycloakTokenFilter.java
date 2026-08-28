package com.moreira.order_service.filters;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class KeycloakTokenFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATIONHEADER = "Authorization";
    private final KeycloakTokenValidator tokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATIONHEADER);

        if (authHeader != null && authHeader.startsWith(BEARER)) {
            try {
                String token = authHeader.substring(BEARER.length());
                String username = tokenValidator.validate(token);

                log.info("username:{}", username);

                if (username != null) {
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        setContext(request, username);
                    }
                } else {
                    log.info("Invalid Request: Token is expired or tampered");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Token is expired or tampered");
                    return;
                }
            } catch (Exception e) {
                log.error("Token validation error: ", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Error validating token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setContext(HttpServletRequest request, String username) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, null);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        log.debug("authenticated user {}, setting security context", username);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("username", username);
    }

    public KeycloakTokenFilter(KeycloakTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

}
