package sn.ipd.gestion_scolaire.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sn.ipd.gestion_scolaire.entity.Etudiant;

import java.util.Optional;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long>, JpaSpecificationExecutor<Etudiant> {

    Optional<Etudiant> findByMatricule(String matricule);

    Optional<Etudiant> findByEmail(String email);

    Optional<Etudiant> findByUserId(Long userId);

    boolean existsByMatricule(String matricule);

    boolean existsByEmail(String email);

    Page<Etudiant> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
            String firstName, String lastName, String matricule, Pageable pageable);
}
