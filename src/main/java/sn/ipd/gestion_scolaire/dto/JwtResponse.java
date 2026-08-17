package sn.ipd.gestion_scolaire.dto;

import java.util.List;

public record JwtResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long id,
        String username,
        String email,
        List<String> roles) {
}
