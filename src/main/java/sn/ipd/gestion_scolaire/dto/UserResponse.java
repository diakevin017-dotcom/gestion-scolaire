package sn.ipd.gestion_scolaire.dto;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Instant createdAt,
        Set<String> roles) {
}
