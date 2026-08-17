package sn.ipd.gestion_scolaire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ipd.gestion_scolaire.entity.ERole;
import sn.ipd.gestion_scolaire.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}
