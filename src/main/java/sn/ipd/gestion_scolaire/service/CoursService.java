package sn.ipd.gestion_scolaire.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ipd.gestion_scolaire.dto.CoursRequest;
import sn.ipd.gestion_scolaire.dto.CoursResponse;
import sn.ipd.gestion_scolaire.dto.EnseignantResponse;
import sn.ipd.gestion_scolaire.entity.Cours;
import sn.ipd.gestion_scolaire.entity.Enseignant;
import sn.ipd.gestion_scolaire.exception.ConflictException;
import sn.ipd.gestion_scolaire.exception.ResourceNotFoundException;
import sn.ipd.gestion_scolaire.repository.CoursRepository;
import sn.ipd.gestion_scolaire.repository.EnseignantRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CoursService {

    private final CoursRepository coursRepository;
    private final EnseignantRepository enseignantRepository;
    private final EnseignantService enseignantService;

    public CoursService(CoursRepository coursRepository,
                        EnseignantRepository enseignantRepository,
                        EnseignantService enseignantService) {
        this.coursRepository = coursRepository;
        this.enseignantRepository = enseignantRepository;
        this.enseignantService = enseignantService;
    }

    @Transactional(readOnly = true)
    public Page<CoursResponse> findAll(Pageable pageable) {
        return coursRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CoursResponse findById(Long id) {
        Cours cours = coursRepository.findByIdWithEnseignants(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours", id));
        return toResponse(cours);
    }

    @Transactional
    public CoursResponse create(CoursRequest request) {
        if (coursRepository.existsByCode(request.code())) {
            throw new ConflictException("Le code '" + request.code() + "' existe déjà");
        }
        Cours cours = Cours.builder()
                .nom(request.nom())
                .code(request.code())
                .description(request.description())
                .credits(request.credits())
                .enseignants(new HashSet<>())
                .inscriptions(new ArrayList<>())
                .build();

        if (request.enseignantIds() != null) {
            Set<Enseignant> enseignants = new HashSet<>();
            for (Long id : request.enseignantIds()) {
                enseignants.add(enseignantRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Enseignant", id)));
            }
            cours.setEnseignants(enseignants);
        }

        return toResponse(coursRepository.save(cours));
    }

    @Transactional
    public CoursResponse update(Long id, CoursRequest request) {
        Cours cours = getCours(id);

        if (!cours.getCode().equals(request.code()) && coursRepository.existsByCode(request.code())) {
            throw new ConflictException("Le code '" + request.code() + "' existe déjà");
        }

        cours.setNom(request.nom());
        cours.setCode(request.code());
        cours.setDescription(request.description());
        cours.setCredits(request.credits());

        if (request.enseignantIds() != null) {
            Set<Enseignant> enseignants = new HashSet<>();
            for (Long enseignantId : request.enseignantIds()) {
                enseignants.add(enseignantRepository.findById(enseignantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Enseignant", enseignantId)));
            }
            cours.setEnseignants(enseignants);
        }

        return toResponse(coursRepository.save(cours));
    }

    @Transactional
    public void delete(Long id) {
        Cours cours = getCours(id);
        coursRepository.delete(cours);
    }

    public Cours getCours(Long id) {
        return coursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cours", id));
    }

    public CoursResponse toResponse(Cours cours) {
        List<EnseignantResponse> enseignants = cours.getEnseignants().stream()
                .map(enseignantService::toResponse)
                .toList();
        long nbInscriptions = cours.getInscriptions() != null ? cours.getInscriptions().size() : 0;
        return new CoursResponse(
                cours.getId(),
                cours.getNom(),
                cours.getCode(),
                cours.getDescription(),
                cours.getCredits(),
                enseignants,
                nbInscriptions
        );
    }
}

