package sn.ipd.gestion_scolaire.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.ipd.gestion_scolaire.dto.InscriptionRequest;
import sn.ipd.gestion_scolaire.dto.InscriptionResponse;
import sn.ipd.gestion_scolaire.dto.MessageResponse;
import sn.ipd.gestion_scolaire.entity.StatutInscription;
import sn.ipd.gestion_scolaire.service.InscriptionService;

import java.util.List;

@RestController
@RequestMapping("/inscriptions")
public class InscriptionController {

    private final InscriptionService inscriptionService;

    public InscriptionController(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<Page<InscriptionResponse>> findAll(
            @PageableDefault(size = 10, sort = "dateInscription", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(inscriptionService.findAll(pageable));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<List<InscriptionResponse>> findByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(inscriptionService.findByEtudiant(etudiantId));
    }

    @GetMapping("/cours/{coursId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<List<InscriptionResponse>> findByCours(@PathVariable Long coursId) {
        return ResponseEntity.ok(inscriptionService.findByCours(coursId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InscriptionResponse> create(@Valid @RequestBody InscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inscriptionService.create(request));
    }

    @PostMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InscriptionResponse> updateStatut(@PathVariable Long id,
                                                            @RequestParam StatutInscription statut) {
        return ResponseEntity.ok(inscriptionService.updateStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        inscriptionService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Inscription supprimée avec succès"));
    }
}

