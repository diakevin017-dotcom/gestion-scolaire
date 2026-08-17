package sn.ipd.gestion_scolaire.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Le refresh token est obligatoire") String refreshToken) {
}
