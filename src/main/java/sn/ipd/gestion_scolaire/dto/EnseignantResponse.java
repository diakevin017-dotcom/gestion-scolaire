package sn.ipd.gestion_scolaire.dto;

public record EnseignantResponse(
        Long id,
        String matricule,
        String firstName,
        String lastName,
        String email,
        String specialite,
        String photoUrl,
        Long userId,
        int nbCours) {
}
