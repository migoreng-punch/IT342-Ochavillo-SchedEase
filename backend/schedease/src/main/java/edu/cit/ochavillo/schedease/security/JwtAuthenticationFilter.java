package edu.cit.ochavillo.schedease.security;

import java.io.IOException;

import edu.cit.ochavillo.schedease.util.ApiErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.cit.ochavillo.schedease.user.entity.User;
import edu.cit.ochavillo.schedease.user.repository.UserRepository;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Fast-pass for OPTIONS preflight requests
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            try {
                username = jwtUtil.extractUsername(token);

            } catch (ExpiredJwtException e) {
                System.out.println("JWT expired: " + e.getMessage());
                // 🚨 1. Write the specific JSON error
                sendSpecificError(response, "AUTH-002", "Your access token has expired.");
                // 🚨 2. HALT THE CHAIN! Do not let the request continue.
                return;

            } catch (JwtException e) {
                System.out.println("Invalid JWT: " + e.getMessage());
                sendSpecificError(response, "AUTH-005", "Invalid or malformed token.");
                return;

            } catch (Exception e) {
                System.out.println("Token processing error: " + e.getMessage());
                sendSpecificError(response, "AUTH-007", "An error occurred processing your token.");
                return;
            }
        }

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null && jwtUtil.isTokenValid(token)) {

                // 🚨 NOTE: Make sure you pass the FULL 'user' object here,
                // not 'user.getUsername()', so your Controllers can access it!
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );

                System.out.println("🚨 SPRING SECURITY SEES THESE ROLES: " + authentication.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        // If the token was perfectly valid (or if no token was provided at all),
        // the request cleanly proceeds down the chain.
        filterChain.doFilter(request, response);
    }

    // =====================================================================
    // 🚨 THE HELPER METHOD: Converts your Java object to JSON on the fly
    // =====================================================================
    private void sendSpecificError(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Creates your standard Error structure
        ApiErrorResponse errorResponse = new ApiErrorResponse(code, message);

        // Translates the Java object into a JSON string and writes it to the HTTP response
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}