package edu.cit.ochavillo.schedease.security;

import java.io.IOException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.cit.ochavillo.schedease.entity.User;
import edu.cit.ochavillo.schedease.repository.UserRepository;

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

        // OPTIONAL BUT RECOMMENDED: Fast-pass for OPTIONS preflight requests
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            // 🚨 FIX: Wrap the parsing logic in a try-catch to prevent server crashes
            try {
                username = jwtUtil.extractUsername(token);
            } catch (ExpiredJwtException e) {
                // Token is expired. We catch the error so it doesn't crash the server.
                System.out.println("JWT expired: " + e.getMessage());
            } catch (JwtException e) {
                // Catches manipulated or malformed tokens
                System.out.println("Invalid JWT: " + e.getMessage());
            }
        }

        // If the token was expired, username will be null, and this block is safely skipped
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null && jwtUtil.isTokenValid(token)) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                null,
                                user.getAuthorities() // Note: Add roles/authorities here later if needed!
                        );

                System.out.println("🚨 SPRING SECURITY SEES THESE ROLES: " + authentication.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        // The request now cleanly proceeds down the chain!
        filterChain.doFilter(request, response);
    }
}