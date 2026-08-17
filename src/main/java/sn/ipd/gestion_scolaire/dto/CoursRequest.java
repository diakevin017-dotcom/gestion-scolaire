package sn.ipd.gestion_scolaire.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CoursRequest(
        @NotBlank(message = "Le nom du cours est obligatoire") @Size(max = 120) String nom,

        @NotBlank(message = "Le code est obligatoire") @Size(min = 2, max = 10, message = "Le code doit contenir entre 2 et 10 caractères") String code,

        @Size(max = 500) String description,

        @Min(value = 1, message = "Les crédits doivent être >= 1") @Max(value = 30, message = "Les crédits doivent être <= 30") int credits,

        Set<Long> enseignantIds) {
}
