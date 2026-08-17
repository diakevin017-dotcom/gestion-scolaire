package sn.ipd.gestion_scolaire.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sn.ipd.gestion_scolaire.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service de création et validation des access tokens JWT.
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), userDetails.getAuthorities().toString(), expirationMs);
    }

    /**
     * Génère un token à partir de l'entité {@link User} (rôles dérivés en
     * ROLE_xxx).
     */
    public String generateToken(User user) {
        String scope = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName().name())
                .collect(Collectors.joining(",", "[", "]"));
        return buildToken(user.getUsername(), scope, expirationMs);
    }

    public String generateToken(UserDetails userDetails, long ttlMs) {
        return buildToken(userDetails.getUsername(), userDetails.getAuthorities().toString(), ttlMs);
    }

    private String buildToken(String subject, String scope, long ttlMs) {
        return Jwts.builder()
                .subject(subject)
                .claims(Map.of("scope", scope))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
