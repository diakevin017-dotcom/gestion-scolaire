package sn.ipd.gestion_scolaire.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.ipd.gestion_scolaire.entity.Enseignant;

import java.util.Optional;

public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {

    Optional<Enseignant> findByMatricule(String matricule);

    Optional<Enseignant> findByEmail(String email);

    Optional<Enseignant> findByUserId(Long userId);

    boolean existsByMatricule(String matricule);

    boolean existsByEmail(String email);

    Page<Enseignant> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
            String firstName, String lastName, String matricule, Pageable pageable);
}
