package sn.ipd.gestion_scolaire.dto;

import java.util.List;

public record CoursResponse(
        Long id,
        String nom,
        String code,
        String description,
        int credits,
        List<EnseignantResponse> enseignants,
        long nbInscriptions) {
}
