package sn.ipd.gestion_scolaire.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.ipd.gestion_scolaire.entity.Cours;
import sn.ipd.gestion_scolaire.entity.ERole;
import sn.ipd.gestion_scolaire.entity.Enseignant;
import sn.ipd.gestion_scolaire.entity.Etudiant;
import sn.ipd.gestion_scolaire.entity.Inscription;
import sn.ipd.gestion_scolaire.entity.Role;
import sn.ipd.gestion_scolaire.entity.StatutInscription;
import sn.ipd.gestion_scolaire.entity.User;
import sn.ipd.gestion_scolaire.repository.CoursRepository;
import sn.ipd.gestion_scolaire.repository.EnseignantRepository;
import sn.ipd.gestion_scolaire.repository.EtudiantRepository;
import sn.ipd.gestion_scolaire.repository.InscriptionRepository;
import sn.ipd.gestion_scolaire.repository.RoleRepository;
import sn.ipd.gestion_scolaire.repository.UserRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Initialisation de la base avec des données de test (si vide).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final EnseignantRepository enseignantRepository;
    private final CoursRepository coursRepository;
    private final InscriptionRepository inscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
            UserRepository userRepository,
            EtudiantRepository etudiantRepository,
            EnseignantRepository enseignantRepository,
            CoursRepository coursRepository,
            InscriptionRepository inscriptionRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.etudiantRepository = etudiantRepository;
        this.enseignantRepository = enseignantRepository;
        this.coursRepository = coursRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Base de données déjà initialisée, seed ignoré.");
            return;
        }

        log.info("Initialisation des données de démonstration...");

        // 1. Rôles
        Role adminRole = roleRepository.save(new Role(ERole.ADMIN));
        Role ensRole = roleRepository.save(new Role(ERole.ENSEIGNANT));
        Role etuRole = roleRepository.save(new Role(ERole.ETUDIANT));

        // 2. Compte administrateur
        User admin = User.builder()
                .username("admin")
                .email("admin@edupus.sn")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Système")
                .lastName("Administrateur")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(admin);

        // 3. Enseignants (avec comptes)
        Enseignant ens1 = createEnseignant("ENS-001", "Awa", "Diop", "awa.diop@edupus.sn", "Mathématiques", ensRole);
        Enseignant ens2 = createEnseignant("ENS-002", "Moussa", "Ndiaye", "moussa.ndiaye@edupus.sn", "Informatique",
                ensRole);
        Enseignant ens3 = createEnseignant("ENS-003", "Fatou", "Sarr", "fatou.sarr@edupus.sn", "Physique-Chimie",
                ensRole);

        // 4. Étudiants (avec comptes)
        Etudiant etu1 = createEtudiant("ETU-2025-001", "Ousmane", "Ba", "ousmane.ba@edupus.sn",
                LocalDate.of(2004, 3, 12), "L3", etuRole);
        Etudiant etu2 = createEtudiant("ETU-2025-002", "Mariama", "Faye", "mariama.faye@edupus.sn",
                LocalDate.of(2005, 7, 25), "L3", etuRole);
        Etudiant etu3 = createEtudiant("ETU-2025-003", "Ibrahima", "Gueye", "ibrahima.gueye@edupus.sn",
                LocalDate.of(2003, 11, 30), "L2", etuRole);
        Etudiant etu4 = createEtudiant("ETU-2025-004", "Aissatou", "Sy", "aissatou.sy@edupus.sn",
                LocalDate.of(2004, 1, 18), "L3", etuRole);
        Etudiant etu5 = createEtudiant("ETU-2025-005", "Cheikh", "Diallo", "cheikh.diallo@edupus.sn",
                LocalDate.of(2005, 9, 2), "L1", etuRole);

        // 5. Cours
        Cours c1 = createCours("Programmation Java", "JAVA-101", "Introduction à Java et POO", 6, Set.of(ens1));
        Cours c2 = createCours("Développement Web", "WEB-201", "Spring Boot et APIs REST", 6, Set.of(ens2));
        Cours c3 = createCours("Bases de Données", "BDD-101", "SQL et conception relationnelle", 4, Set.of(ens2));

        // 6. Inscriptions
        inscriptionRepository.save(buildInscription(etu1, c1));
        inscriptionRepository.save(buildInscription(etu1, c2));
        inscriptionRepository.save(buildInscription(etu2, c2));
        inscriptionRepository.save(buildInscription(etu3, c3));
        inscriptionRepository.save(buildInscription(etu4, c1));
        inscriptionRepository.save(buildInscription(etu5, c3));

        log.info("Seed terminé : 1 admin, 3 enseignants, 5 étudiants, 3 cours, 6 inscriptions.");
    }

    private Enseignant createEnseignant(String matricule, String first, String last, String email,
            String specialite, Role role) {
        User user = User.builder()
                .username(matricule.toLowerCase())
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .firstName(first)
                .lastName(last)
                .enabled(true)
                .roles(Set.of(role))
                .build();
        userRepository.save(user);

        Enseignant ens = Enseignant.builder()
                .matricule(matricule)
                .firstName(first)
                .lastName(last)
                .email(email)
                .specialite(specialite)
                .user(user)
                .cours(new HashSet<>())
                .build();
        return enseignantRepository.save(ens);
    }

    private Etudiant createEtudiant(String matricule, String first, String last, String email,
            LocalDate dateNaissance, String niveau, Role role) {
        User user = User.builder()
                .username(matricule.toLowerCase())
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .firstName(first)
                .lastName(last)
                .enabled(true)
                .roles(Set.of(role))
                .build();
        userRepository.save(user);

        Etudiant etu = Etudiant.builder()
                .matricule(matricule)
                .firstName(first)
                .lastName(last)
                .email(email)
                .dateNaissance(dateNaissance)
                .telephone("77-000-00-00")
                .niveau(niveau)
                .user(user)
                .inscriptions(new java.util.ArrayList<>())
                .build();
        return etudiantRepository.save(etu);
    }

    private Cours createCours(String nom, String code, String description, int credits, Set<Enseignant> enseignants) {
        Cours cours = Cours.builder()
                .nom(nom)
                .code(code)
                .description(description)
                .credits(credits)
                .enseignants(enseignants)
                .inscriptions(new java.util.ArrayList<>())
                .build();
        return coursRepository.save(cours);
    }

    private Inscription buildInscription(Etudiant etudiant, Cours cours) {
        return Inscription.builder()
                .etudiant(etudiant)
                .cours(cours)
                .dateInscription(LocalDate.now().minusDays(30))
                .statut(StatutInscription.ACTIVE)
                .build();
    }
}
