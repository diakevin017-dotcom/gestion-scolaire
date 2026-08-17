package sn.ipd.gestion_scolaire.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sn.ipd.gestion_scolaire.dto.EtudiantRequest;
import sn.ipd.gestion_scolaire.dto.EtudiantResponse;
import sn.ipd.gestion_scolaire.entity.Etudiant;
import sn.ipd.gestion_scolaire.exception.BadRequestException;
import sn.ipd.gestion_scolaire.exception.ConflictException;
import sn.ipd.gestion_scolaire.exception.ResourceNotFoundException;
import sn.ipd.gestion_scolaire.repository.EtudiantRepository;

import java.util.List;

@Service
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final FileStorageService fileStorageService;

    public EtudiantService(EtudiantRepository etudiantRepository, FileStorageService fileStorageService) {
        this.etudiantRepository = etudiantRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<EtudiantResponse> findAll(Pageable pageable, String search) {
        Page<Etudiant> page;
        if (search != null && !search.isBlank()) {
            page = etudiantRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
                            search, search, search, pageable);
        } else {
            page = etudiantRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EtudiantResponse findById(Long id) {
        return toResponse(getEtudiant(id));
    }

    @Transactional
    public EtudiantResponse create(EtudiantRequest request) {
        if (etudiantRepository.existsByMatricule(request.matricule())) {
            throw new ConflictException("Le matricule '" + request.matricule() + "' existe déjà");
        }
        if (etudiantRepository.existsByEmail(request.email())) {
            throw new ConflictException("L'email '" + request.email() + "' existe déjà");
        }

        Etudiant etudiant = Etudiant.builder()
                .matricule(request.matricule())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .dateNaissance(request.dateNaissance())
                .telephone(request.telephone())
                .niveau(request.niveau())
                .build();

        return toResponse(etudiantRepository.save(etudiant));
    }

    @Transactional
    public EtudiantResponse update(Long id, EtudiantRequest request) {
        Etudiant etudiant = getEtudiant(id);

        if (!etudiant.getMatricule().equals(request.matricule())
                && etudiantRepository.existsByMatricule(request.matricule())) {
            throw new ConflictException("Le matricule '" + request.matricule() + "' existe déjà");
        }
        if (!etudiant.getEmail().equals(request.email()) && etudiantRepository.existsByEmail(request.email())) {
            throw new ConflictException("L'email '" + request.email() + "' existe déjà");
        }

        etudiant.setMatricule(request.matricule());
        etudiant.setFirstName(request.firstName());
        etudiant.setLastName(request.lastName());
        etudiant.setEmail(request.email());
        etudiant.setDateNaissance(request.dateNaissance());
        etudiant.setTelephone(request.telephone());
        etudiant.setNiveau(request.niveau());

        return toResponse(etudiantRepository.save(etudiant));
    }

    @Transactional
    public void delete(Long id) {
        Etudiant etudiant = getEtudiant(id);
        etudiantRepository.delete(etudiant);
    }

    @Transactional
    public EtudiantResponse uploadPhoto(Long id, MultipartFile file) {
        Etudiant etudiant = getEtudiant(id);
        String url = fileStorageService.storePhoto(file);
        etudiant.setPhotoUrl(url);
        return toResponse(etudiantRepository.save(etudiant));
    }

    @Transactional
    public EtudiantResponse uploadDocument(Long id, MultipartFile file) {
        Etudiant etudiant = getEtudiant(id);
        String url = fileStorageService.storeDocument(file);
        etudiant.setDocumentUrl(url);
        return toResponse(etudiantRepository.save(etudiant));
    }

    public Etudiant getEtudiant(Long id) {
        return etudiantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", id));
    }

    public EtudiantResponse toResponse(Etudiant etudiant) {
        return new EtudiantResponse(
                etudiant.getId(),
                etudiant.getMatricule(),
                etudiant.getFirstName(),
                etudiant.getLastName(),
                etudiant.getEmail(),
                etudiant.getDateNaissance(),
                etudiant.getTelephone(),
                etudiant.getNiveau(),
                etudiant.getPhotoUrl(),
                etudiant.getDocumentUrl(),
                etudiant.getUser() != null ? etudiant.getUser().getId() : null);
    }
}
