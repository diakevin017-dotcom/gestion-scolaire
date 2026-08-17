package sn.ipd.gestion_scolaire.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ipd.gestion_scolaire.entity.RefreshToken;
import sn.ipd.gestion_scolaire.entity.User;
import sn.ipd.gestion_scolaire.exception.TokenRefreshException;
import sn.ipd.gestion_scolaire.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Gestion des refresh tokens avec rotation : chaque renouvellement
 * invalide l'ancien token et en émet un nouveau.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Rotation : supprimer l'ancien refresh token (OneToOne interdit la
        // coexistence de deux entités avec le même user_id)
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresent(existing -> {
                    user.setRefreshToken(null);
                    refreshTokenRepository.delete(existing);
                    refreshTokenRepository.flush();
                });

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""))
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        user.setRefreshToken(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token invalide"));

        if (refreshToken.isRevoked()) {
            throw new TokenRefreshException("Refresh token révoqué");
        }
        if (refreshToken.isExpired()) {
            throw new TokenRefreshException("Refresh token expiré");
        }
        return refreshToken;
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.findByUserId(userId).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }
}
