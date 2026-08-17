package sn.ipd.gestion_scolaire.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnseignantRequest(
        @NotBlank(message = "Le matricule est obligatoire") @Size(max = 20) String matricule,

        @NotBlank(message = "Le prénom est obligatoire") @Size(max = 50) String firstName,

        @NotBlank(message = "Le nom est obligatoire") @Size(max = 50) String lastName,

        @NotBlank(message = "L'email est obligatoire") @Email(message = "L'email doit être valide") @Size(max = 120) String email,

        @Size(max = 100) String specialite) {
}
