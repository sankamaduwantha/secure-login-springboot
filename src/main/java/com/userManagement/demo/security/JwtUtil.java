package com.userManagement.demo.security;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secreteKey;
    
    @Value("${jwt.expiration}")
    private Long jwtexpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secreteKey.getBytes());
    }

    public String generateToken(String email){
        Date now = new Date();
        Date expirydate = new Date(now.getTime()+jwtexpiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expirydate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token){
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public boolean isTokenValid(String token, String email){
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
