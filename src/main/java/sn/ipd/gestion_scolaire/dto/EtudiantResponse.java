package sn.ipd.gestion_scolaire.dto;

import java.time.LocalDate;

public record EtudiantResponse(
        Long id,
        String matricule,
        String firstName,
        String lastName,
        String email,
        LocalDate dateNaissance,
        String telephone,
        String niveau,
        String photoUrl,
        String documentUrl,
        Long userId) {
}
