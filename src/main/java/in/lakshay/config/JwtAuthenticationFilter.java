package in.lakshay.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// filter that checks JWT tokens and authenticates users
@Component
@Slf4j // for logging
public class JwtAuthenticationFilter extends OncePerRequestFilter { // runs once per request
    @Autowired
    private JwtUtil jwtUtil; // helper for JWT operations

    // this runs for every request that passes through the filter chain
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.debug("Request URI: {}", requestURI);

        // special case - stripe webhooks don't have auth headers
        // they use their own auth mechanism (webhook secret)
        if (requestURI.equals("/api/v1/payments/webhook")) {
            log.debug("Skipping auth for stripe webhook");
            filterChain.doFilter(request, response);
            return;
        }

        // look for the auth header with the JWT
        String authHeader = request.getHeader("Authorization");
        log.debug("Auth header present: {}", authHeader != null);

        // no auth header = anonymous request
        if (authHeader == null) {
            log.debug("No auth header, continuing as anonymous");
            filterChain.doFilter(request, response);
            return;
        }

        // we only support bearer tokens
        if (!authHeader.startsWith("Bearer ")) {
            log.debug("Not a bearer token, ignoring");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // strip off the "Bearer " prefix
            String token = authHeader.substring(7);

            // check if token is still valid
            if (!jwtUtil.isTokenExpired(token)) {
                // extract user info from token
                String username = jwtUtil.getUsernameFromToken(token);
                List<String> roles = jwtUtil.getRolesFromToken(token);

                log.debug("User from token: {}", username);
                log.debug("Roles: {}", roles);

                // convert roles to spring security authorities
                // make sure they have ROLE_ prefix that spring security expects
                List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                    .collect(Collectors.toList());

                log.debug("Auth objects created: {}", authorities);

                // create auth token - no credentials needed since JWT already validated
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // store in security context - this is what makes the user "logged in"
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("User authenticated: {}", authentication);
                log.debug("Current auth: {}", SecurityContextHolder.getContext().getAuthentication());
                log.debug("User authorities: {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            } else {
                // token expired but we'll just continue as anonymous
                log.debug("Token expired, continuing as anonymous");
            }
        } catch (Exception e) {
            // something went wrong with the token - clear security context
            log.error("JWT auth failed: ", e);
            SecurityContextHolder.clearContext(); // reset auth state
        }

        // always continue the filter chain
        filterChain.doFilter(request, response);
    }

    // TODO: add token blacklist for logout functionality
}