package sn.ipd.gestion_scolaire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.ipd.gestion_scolaire.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
