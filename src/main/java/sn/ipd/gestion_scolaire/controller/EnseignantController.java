package sn.ipd.gestion_scolaire.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sn.ipd.gestion_scolaire.dto.EnseignantRequest;
import sn.ipd.gestion_scolaire.dto.EnseignantResponse;
import sn.ipd.gestion_scolaire.dto.MessageResponse;
import sn.ipd.gestion_scolaire.service.EnseignantService;

@RestController
@RequestMapping("/enseignants")
public class EnseignantController {

    private final EnseignantService enseignantService;

    public EnseignantController(EnseignantService enseignantService) {
        this.enseignantService = enseignantService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<Page<EnseignantResponse>> findAll(
            @PageableDefault(size = 10, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(enseignantService.findAll(pageable, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<EnseignantResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(enseignantService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnseignantResponse> create(@Valid @RequestBody EnseignantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enseignantService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnseignantResponse> update(@PathVariable Long id, @Valid @RequestBody EnseignantRequest request) {
        return ResponseEntity.ok(enseignantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        enseignantService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Enseignant supprimé avec succès"));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnseignantResponse> uploadPhoto(@PathVariable Long id,
                                                         @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(enseignantService.uploadPhoto(id, file));
    }
}

