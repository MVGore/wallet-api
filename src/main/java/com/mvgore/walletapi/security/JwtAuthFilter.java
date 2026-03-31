package com.mvgore.walletapi.security;

import com.mvgore.walletapi.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth/")
                || path.equals("/api/user/forgot-password")
                || path.equals("/api/user/reset-password");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

//        System.out.println("JWT FILTER CALLED");
//        System.out.println("Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            if (jwtUtil.validateToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                Long userId = jwtUtil.extractUserId(token);
                Integer tokenVersionFromJwt = jwtUtil.extractTokenVersion(token);


                UserDetails userDetailsGeneric  = userDetailsService.loadUserById(userId);

                CustomUserDetails userDetails = (CustomUserDetails) userDetailsGeneric;

//                System.out.println("DB tokenVersion: " + userDetails.getTokenVersion());
//                System.out.println("JWT tokenVersion: " + tokenVersionFromJwt);

                if (!((CustomUserDetails) userDetails).getTokenVersion().equals(tokenVersionFromJwt)) {

                    SecurityContextHolder.clearContext();

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token is invalid (user logged out or token revoked)");
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

//                System.out.println("Authorities: " + userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }catch (Exception e) {
//            System.out.println("JWT ERROR: " + e.getMessage());

            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("{\"message\": \"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}