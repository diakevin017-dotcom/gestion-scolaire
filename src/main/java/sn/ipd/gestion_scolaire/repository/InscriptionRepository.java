package sn.ipd.gestion_scolaire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.ipd.gestion_scolaire.entity.Inscription;
import sn.ipd.gestion_scolaire.entity.StatutInscription;

import java.util.List;
import java.util.Optional;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    Optional<Inscription> findByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    boolean existsByEtudiantIdAndCoursId(Long etudiantId, Long coursId);

    @Query("select i from Inscription i join fetch i.cours where i.etudiant.id = :etudiantId order by i.dateInscription desc")
    List<Inscription> findAllByEtudiantId(@Param("etudiantId") Long etudiantId);

    @Query("select i from Inscription i join fetch i.etudiant where i.cours.id = :coursId")
    List<Inscription> findAllByCoursId(@Param("coursId") Long coursId);

    List<Inscription> findByStatut(StatutInscription statut);

    @Modifying
    @Query("update Inscription i set i.statut = :statut where i.id = :id")
    int updateStatut(@Param("id") Long id, @Param("statut") StatutInscription statut);
}
