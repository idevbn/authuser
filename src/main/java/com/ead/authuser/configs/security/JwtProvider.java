package com.ead.authuser.configs.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.Collectors;

@Log4j2
@Component
public class JwtProvider {

    @Value("${ead.auth.jwtSecret}")
    private String jwtSecret;

    @Value("${ead.auth.jwtExpirationMs}")
    private String jwtExpirationMs;

    public String generateJwt(final Authentication authentication) {
        final UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        final Instant issuedAt = Instant.now();

        final String roles = userPrincipal
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        final SecretKey key = Keys.hmacShaKeyFor(this.jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userPrincipal.getUserId().toString())
                .claim("roles", roles)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(issuedAt.plus(Long.parseLong(this.jwtExpirationMs), ChronoUnit.MILLIS)))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getSubjectJwt(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(this.jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwt(final String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(this.jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(authToken);

            return true;
        } catch (final SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (final MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (final ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch(final UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (final IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

}
