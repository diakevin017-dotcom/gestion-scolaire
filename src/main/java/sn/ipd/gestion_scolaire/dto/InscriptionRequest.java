package sn.ipd.gestion_scolaire.dto;

import jakarta.validation.constraints.NotNull;

public record InscriptionRequest(
        @NotNull(message = "L'id de l'étudiant est obligatoire") Long etudiantId,

        @NotNull(message = "L'id du cours est obligatoire") Long coursId) {
}
