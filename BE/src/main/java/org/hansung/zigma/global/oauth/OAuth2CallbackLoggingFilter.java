package org.hansung.zigma.global.oauth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OAuth2CallbackLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuth2CallbackLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/login/oauth2/code/")) {
            log.info("OAuth callback request uri={}, query={}, cookies={}",
                    request.getRequestURI(),
                    request.getQueryString(),
                    request.getHeader("Cookie"));
        }

        filterChain.doFilter(request, response);
    }
}
