package sn.ipd.gestion_scolaire.exception;

/**
 * Levée lors d'un refresh token invalide, expiré ou révoqué.
 */
public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String message) {
        super(message);
    }
}
