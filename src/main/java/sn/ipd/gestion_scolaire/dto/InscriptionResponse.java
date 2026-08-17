package sn.ipd.gestion_scolaire.dto;

import java.time.LocalDate;

public record InscriptionResponse(
        Long id,
        Long etudiantId,
        String etudiantNom,
        Long coursId,
        String coursNom,
        String coursCode,
        LocalDate dateInscription,
        String statut,
        Double note) {
}
