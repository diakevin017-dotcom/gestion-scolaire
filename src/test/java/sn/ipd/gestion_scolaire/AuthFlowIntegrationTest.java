package sn.ipd.gestion_scolaire;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sn.ipd.gestion_scolaire.dto.JwtResponse;
import sn.ipd.gestion_scolaire.dto.LoginRequest;
import sn.ipd.gestion_scolaire.dto.RegisterRequest;
import sn.ipd.gestion_scolaire.exception.TokenRefreshException;
import sn.ipd.gestion_scolaire.repository.RefreshTokenRepository;
import sn.ipd.gestion_scolaire.service.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test d'intégration de la couche service : inscription, connexion,
 * rotation du refresh token et révocation.
 */
@SpringBootTest
@Transactional
class AuthFlowIntegrationTest {

        @Autowired
        private AuthService authService;

        @Autowired
        private RefreshTokenRepository refreshTokenRepository;

        @Test
        void register_login_refresh_rotation_and_logout() {
                // 1. Inscription (rôle ETUDIANT)
                JwtResponse registered = authService.register(new RegisterRequest(
                                "etudiant_test", "etudiant.test@edupus.sn", "password123", "Test", "Etudiant"));
                assertThat(registered.accessToken()).isNotBlank();
                assertThat(registered.refreshToken()).isNotBlank();
                assertThat(registered.roles()).containsExactly("ETUDIANT");

                // 2. Connexion → nouvelle paire de jetons (rotation du refresh précédent)
                JwtResponse login = authService.login(new LoginRequest("etudiant_test", "password123"));
                assertThat(login.accessToken()).isNotBlank();
                String refresh2 = login.refreshToken();
                assertThat(refreshTokenRepository.findByToken(refresh2)).isPresent();

                // Le refresh token émis à l'inscription a été supprimé (rotation)
                assertThat(refreshTokenRepository.findByToken(registered.refreshToken())).isEmpty();

                // 3. Refresh → rotation : nouveau refresh token, l'ancien supprimé
                JwtResponse refreshed = authService.refresh(refresh2);
                assertThat(refreshed.refreshToken()).isNotEqualTo(refresh2);
                assertThat(refreshed.accessToken()).isNotBlank();
                assertThat(refreshTokenRepository.findByToken(refresh2)).isEmpty();

                // 4. Rejouer l'ancien refresh token → exception (token inexistant)
                assertThatThrownBy(() -> authService.refresh(refresh2))
                                .isInstanceOf(TokenRefreshException.class);

                // 5. Logout → révocation du refresh token courant
                authService.logout(refreshed.refreshToken());
                assertThat(refreshTokenRepository.findByToken(refreshed.refreshToken()).orElseThrow().isRevoked())
                                .isTrue();

                // 6. Utiliser un refresh token révoqué échoue
                assertThatThrownBy(() -> authService.refresh(refreshed.refreshToken()))
                                .isInstanceOf(TokenRefreshException.class);
        }
}
