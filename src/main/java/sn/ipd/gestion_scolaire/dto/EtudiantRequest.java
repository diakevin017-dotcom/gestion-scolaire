package sn.ipd.gestion_scolaire.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EtudiantRequest(
                @NotBlank(message = "Le matricule est obligatoire") @Size(max = 20) String matricule,

                @NotBlank(message = "Le prénom est obligatoire") @Size(max = 50) String firstName,

                @NotBlank(message = "Le nom est obligatoire") @Size(max = 50) String lastName,

                @NotBlank(message = "L'email est obligatoire") @Email(message = "L'email doit être valide") @Size(max = 120) String email,

                @NotNull(message = "La date de naissance est obligatoire") @Past(message = "La date de naissance doit être dans le passé") LocalDate dateNaissance,

                @Size(max = 30) String telephone,

                @Size(max = 20) String niveau) {
}
