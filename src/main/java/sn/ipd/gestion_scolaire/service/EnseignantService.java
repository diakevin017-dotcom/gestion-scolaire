package sn.ipd.gestion_scolaire.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sn.ipd.gestion_scolaire.dto.EnseignantRequest;
import sn.ipd.gestion_scolaire.dto.EnseignantResponse;
import sn.ipd.gestion_scolaire.entity.Enseignant;
import sn.ipd.gestion_scolaire.exception.ConflictException;
import sn.ipd.gestion_scolaire.exception.ResourceNotFoundException;
import sn.ipd.gestion_scolaire.repository.EnseignantRepository;

@Service
public class EnseignantService {

    private final EnseignantRepository enseignantRepository;
    private final FileStorageService fileStorageService;

    public EnseignantService(EnseignantRepository enseignantRepository, FileStorageService fileStorageService) {
        this.enseignantRepository = enseignantRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<EnseignantResponse> findAll(Pageable pageable, String search) {
        Page<Enseignant> page;
        if (search != null && !search.isBlank()) {
            page = enseignantRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
                            search, search, search, pageable);
        } else {
            page = enseignantRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EnseignantResponse findById(Long id) {
        return toResponse(getEnseignant(id));
    }

    @Transactional
    public EnseignantResponse create(EnseignantRequest request) {
        if (enseignantRepository.existsByMatricule(request.matricule())) {
            throw new ConflictException("Le matricule '" + request.matricule() + "' existe déjà");
        }
        if (enseignantRepository.existsByEmail(request.email())) {
            throw new ConflictException("L'email '" + request.email() + "' existe déjà");
        }

        Enseignant enseignant = Enseignant.builder()
                .matricule(request.matricule())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .specialite(request.specialite())
                .build();

        return toResponse(enseignantRepository.save(enseignant));
    }

    @Transactional
    public EnseignantResponse update(Long id, EnseignantRequest request) {
        Enseignant enseignant = getEnseignant(id);

        if (!enseignant.getMatricule().equals(request.matricule())
                && enseignantRepository.existsByMatricule(request.matricule())) {
            throw new ConflictException("Le matricule '" + request.matricule() + "' existe déjà");
        }
        if (!enseignant.getEmail().equals(request.email()) && enseignantRepository.existsByEmail(request.email())) {
            throw new ConflictException("L'email '" + request.email() + "' existe déjà");
        }

        enseignant.setMatricule(request.matricule());
        enseignant.setFirstName(request.firstName());
        enseignant.setLastName(request.lastName());
        enseignant.setEmail(request.email());
        enseignant.setSpecialite(request.specialite());

        return toResponse(enseignantRepository.save(enseignant));
    }

    @Transactional
    public void delete(Long id) {
        Enseignant enseignant = getEnseignant(id);
        enseignantRepository.delete(enseignant);
    }

    @Transactional
    public EnseignantResponse uploadPhoto(Long id, MultipartFile file) {
        Enseignant enseignant = getEnseignant(id);
        String url = fileStorageService.storePhoto(file);
        enseignant.setPhotoUrl(url);
        return toResponse(enseignantRepository.save(enseignant));
    }

    public Enseignant getEnseignant(Long id) {
        return enseignantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", id));
    }

    public EnseignantResponse toResponse(Enseignant enseignant) {
        return new EnseignantResponse(
                enseignant.getId(),
                enseignant.getMatricule(),
                enseignant.getFirstName(),
                enseignant.getLastName(),
                enseignant.getEmail(),
                enseignant.getSpecialite(),
                enseignant.getPhotoUrl(),
                enseignant.getUser() != null ? enseignant.getUser().getId() : null,
                enseignant.getCours() != null ? enseignant.getCours().size() : 0);
    }
}
