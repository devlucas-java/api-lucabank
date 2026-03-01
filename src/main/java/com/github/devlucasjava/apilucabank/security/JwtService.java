package com.github.devlucasjava.apilucabank.security;

import com.github.devlucasjava.apilucabank.exception.CustomSignatureException;
import com.github.devlucasjava.apilucabank.exception.TokenExpiredException;
import com.github.devlucasjava.apilucabank.model.Users;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    public final Long jwtExpiration;
    private final Key signingKey;

    public JwtService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") Long jwtExpiration
    ) {
        this.jwtExpiration = jwtExpiration;
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Users user) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(jwtExpiration)))
                .addClaims(Map.of(
                        "roles", user.getAuthorities().stream()
                                .map(auth -> auth.getAuthority())
                                .toList()
                ))
                .signWith(signingKey, SignatureAlgorithm.HS384)
                .compact();

        log.debug("Generated JWT for user: {}", user.getUsername());
        return token;
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", token);
            throw new TokenExpiredException("Token expired");
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", token);
            throw new CustomSignatureException("Token invalid");
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, Users user) {
        String username = extractUsername(token);
        boolean valid = username.equals(user.getUsername()) && !isTokenExpired(token);
        log.debug("Token validation for user {}: {}", user.getUsername(), valid);
        return valid;
    }

    public boolean isTokenExpired(String token) {
        Date expiration = parseClaims(token).getExpiration();
        boolean expired = expiration.before(Date.from(Instant.now()));
        log.trace("Token expiration check: {} (expires at {})", expired, expiration);
        return expired;
    }
}