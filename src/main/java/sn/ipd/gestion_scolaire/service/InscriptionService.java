package sn.ipd.gestion_scolaire.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ipd.gestion_scolaire.dto.InscriptionRequest;
import sn.ipd.gestion_scolaire.dto.InscriptionResponse;
import sn.ipd.gestion_scolaire.entity.Cours;
import sn.ipd.gestion_scolaire.entity.Etudiant;
import sn.ipd.gestion_scolaire.entity.Inscription;
import sn.ipd.gestion_scolaire.entity.StatutInscription;
import sn.ipd.gestion_scolaire.exception.BadRequestException;
import sn.ipd.gestion_scolaire.exception.ConflictException;
import sn.ipd.gestion_scolaire.exception.ResourceNotFoundException;
import sn.ipd.gestion_scolaire.repository.CoursRepository;
import sn.ipd.gestion_scolaire.repository.EtudiantRepository;
import sn.ipd.gestion_scolaire.repository.InscriptionRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final EtudiantRepository etudiantRepository;
    private final CoursRepository coursRepository;

    public InscriptionService(InscriptionRepository inscriptionRepository,
            EtudiantRepository etudiantRepository,
            CoursRepository coursRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.etudiantRepository = etudiantRepository;
        this.coursRepository = coursRepository;
    }

    @Transactional(readOnly = true)
    public Page<InscriptionResponse> findAll(Pageable pageable) {
        return inscriptionRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<InscriptionResponse> findByEtudiant(Long etudiantId) {
        return inscriptionRepository.findAllByEtudiantId(etudiantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InscriptionResponse> findByCours(Long coursId) {
        return inscriptionRepository.findAllByCoursId(coursId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InscriptionResponse create(InscriptionRequest request) {
        if (inscriptionRepository.existsByEtudiantIdAndCoursId(request.etudiantId(), request.coursId())) {
            throw new ConflictException("L'étudiant est déjà inscrit à ce cours");
        }
        Etudiant etudiant = etudiantRepository.findById(request.etudiantId())
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant", request.etudiantId()));
        Cours cours = coursRepository.findById(request.coursId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours", request.coursId()));

        Inscription inscription = Inscription.builder()
                .etudiant(etudiant)
                .cours(cours)
                .dateInscription(LocalDate.now())
                .statut(StatutInscription.ACTIVE)
                .build();

        return toResponse(inscriptionRepository.save(inscription));
    }

    @Transactional
    public InscriptionResponse updateStatut(Long id, StatutInscription statut) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", id));
        inscription.setStatut(statut);
        return toResponse(inscriptionRepository.save(inscription));
    }

    @Transactional
    public void delete(Long id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", id));
        inscriptionRepository.delete(inscription);
    }

    public InscriptionResponse toResponse(Inscription inscription) {
        if (inscription.getEtudiant() == null || inscription.getCours() == null) {
            throw new BadRequestException("Inscription incomplète (étudiant ou cours absent)");
        }
        return new InscriptionResponse(
                inscription.getId(),
                inscription.getEtudiant().getId(),
                inscription.getEtudiant().getFirstName() + " " + inscription.getEtudiant().getLastName(),
                inscription.getCours().getId(),
                inscription.getCours().getNom(),
                inscription.getCours().getCode(),
                inscription.getDateInscription(),
                inscription.getStatut() != null ? inscription.getStatut().name() : null,
                inscription.getNote());
    }
}
