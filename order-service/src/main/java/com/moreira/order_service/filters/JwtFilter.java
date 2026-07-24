package com.moreira.order_service.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "Authentication";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = response.getHeader(TOKEN_HEADER);

        if (StringUtils.isEmpty(token)) {

            log.error("Invalid JWT Token");
            // No authenticated
            // SecurityContextHolder.getContext().setAuthentication(null);
            filterChain.doFilter(request, response);
        }
    }
}
