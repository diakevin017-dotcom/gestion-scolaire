package sn.ipd.gestion_scolaire.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ipd.gestion_scolaire.dto.JwtResponse;
import sn.ipd.gestion_scolaire.dto.LoginRequest;
import sn.ipd.gestion_scolaire.dto.RegisterRequest;
import sn.ipd.gestion_scolaire.entity.ERole;
import sn.ipd.gestion_scolaire.entity.RefreshToken;
import sn.ipd.gestion_scolaire.entity.Role;
import sn.ipd.gestion_scolaire.entity.User;
import sn.ipd.gestion_scolaire.exception.BadRequestException;
import sn.ipd.gestion_scolaire.exception.ConflictException;
import sn.ipd.gestion_scolaire.exception.ResourceNotFoundException;
import sn.ipd.gestion_scolaire.exception.TokenRefreshException;
import sn.ipd.gestion_scolaire.repository.RoleRepository;
import sn.ipd.gestion_scolaire.repository.UserRepository;
import sn.ipd.gestion_scolaire.security.JwtService;
import sn.ipd.gestion_scolaire.security.RefreshTokenService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Le username '" + request.username() + "' est déjà pris");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("L'email '" + request.email() + "' est déjà utilisé");
        }

        Role role = roleRepository.findByName(ERole.ETUDIANT)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle ETUDIANT non trouvé"));

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .enabled(true)
                .createdAt(Instant.now())
                .roles(Set.of(role))
                .build();

        userRepository.save(user);
        return buildJwtResponse(user);
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable : " + request.username()));

        return buildJwtResponse(user);
    }

    @Transactional
    public JwtResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenService.verifyRefreshToken(refreshToken);
        User user = stored.getUser();

        // Rotation : création d'un nouveau refresh token (l'ancien est révoqué)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        return new JwtResponse(
                jwtService.generateToken(user),
                newRefreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revokeToken(refreshToken);
        }
    }

    private JwtResponse buildJwtResponse(User user) {
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        return new JwtResponse(
                jwtService.generateToken(user),
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }
}