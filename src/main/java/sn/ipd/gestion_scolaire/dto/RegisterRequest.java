package sn.ipd.gestion_scolaire.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.ipd.gestion_scolaire.entity.Role;

public record RegisterRequest(
        @NotBlank(message = "Le username est obligatoire") @Size(min = 3, max = 50, message = "Le username doit contenir entre 3 et 50 caractères") String username,

        @NotBlank(message = "L'email est obligatoire") @Email(message = "L'email doit être valide") @Size(max = 120) String email,

        @NotBlank(message = "Le mot de passe est obligatoire") @Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caractères") String password,

        @NotBlank(message = "Le prénom est obligatoire") @Size(max = 50) String firstName,

        @NotBlank(message = "Le nom est obligatoire") @Size(max = 50) String lastName,

        @NotNull(message = "Le rôle est obligatoire") Role role) {
}