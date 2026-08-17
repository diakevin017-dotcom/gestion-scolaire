package sn.ipd.gestion_scolaire.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sn.ipd.gestion_scolaire.entity.ERole;
import sn.ipd.gestion_scolaire.entity.User;
import sn.ipd.gestion_scolaire.exception.ResourceNotFoundException;
import sn.ipd.gestion_scolaire.repository.UserRepository;

/**
 * Helper pour accéder à l'utilisateur authentifié courant.
 */
@Service
public class SecurityService {

    private final UserRepository userRepository;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ResourceNotFoundException("Utilisateur non authentifié");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Utilisateur introuvable : " + userDetails.getUsername()));
    }

    public boolean hasRole(ERole role) {
        return getCurrentUser().hasRole(role);
    }
}
