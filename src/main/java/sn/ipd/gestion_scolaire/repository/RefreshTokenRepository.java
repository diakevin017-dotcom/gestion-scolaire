package sn.ipd.gestion_scolaire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ipd.gestion_scolaire.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
