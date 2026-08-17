package sn.ipd.gestion_scolaire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.ipd.gestion_scolaire.entity.Cours;

import java.util.List;
import java.util.Optional;

public interface CoursRepository extends JpaRepository<Cours, Long> {

    Optional<Cours> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select c from Cours c left join fetch c.enseignants where c.id = :id")
    Optional<Cours> findByIdWithEnseignants(@Param("id") Long id);

    @Query("select distinct c from Cours c left join fetch c.enseignants order by c.nom asc")
    List<Cours> findAllWithEnseignants();
}
