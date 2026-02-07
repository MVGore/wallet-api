package com.mvgore.walletapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    // Correct way to generate a secure 256-bit key for HS256
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);  // Securely generated key

    // Method to generate a JWT token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 hours
                .signWith(secretKey)  // Use the generated 256-bit key
                .compact();
    }

    // Method to extract the username from the token
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)  // Use the generated 256-bit key
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Check if the token is expired
    public boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    private Date extractExpirationDate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)  // Use the generated 256-bit key
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    // Validate if the token is valid
    public boolean validateToken(String token) {
        try {
            // Extract username from the token
            String username = extractUsername(token);

            // Check if the token is expired
            if (isTokenExpired(token)) {
                return false;  // Token is expired
            }

            return username != null && !username.isEmpty();
        } catch (Exception e) {
            // Handle any exceptions (e.g., malformed token)
            return false;
        }
    }
}
