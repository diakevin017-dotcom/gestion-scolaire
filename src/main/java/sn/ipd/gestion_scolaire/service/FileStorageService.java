package sn.ipd.gestion_scolaire.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sn.ipd.gestion_scolaire.exception.FileStorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Service de stockage des fichiers (photos, PDF) avec validation MIME
 * basée sur les signatures magiques et contrôle de taille.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_PHOTO_MAGIC = Set.of(
            "\uFFFD\uFFFD", // JPEG 0xFF 0xD8 (lu en texte)
            "\uFFFDPNG" // PNG
    );

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_DOC_EXTENSIONS = Set.of("pdf");

    private final Path uploadRoot;
    private final long photoMaxSize;
    private final long docMaxSize;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir,
            @Value("${app.upload.photo-max-size:2MB}") String photoMaxSize,
            @Value("${app.upload.doc-max-size:5MB}") String docMaxSize) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.photoMaxSize = parseSize(photoMaxSize);
        this.docMaxSize = parseSize(docMaxSize);
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de créer le répertoire d'upload", ex);
        }
    }

    /**
     * Stocke une photo de profil (JPEG/PNG, <= 2 Mo).
     */
    public String storePhoto(MultipartFile file) {
        return store(file, "photos", ALLOWED_IMAGE_EXTENSIONS, photoMaxSize, true);
    }

    /**
     * Stocke un document PDF (<= 5 Mo).
     */
    public String storeDocument(MultipartFile file) {
        return store(file, "docs", ALLOWED_DOC_EXTENSIONS, docMaxSize, false);
    }

    private String store(MultipartFile file, String subDir, Set<String> allowedExt,
            long maxSize, boolean requireImage) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Fichier vide");
        }
        if (file.getSize() > maxSize) {
            throw new FileStorageException(
                    "Fichier trop volumineux. Taille maximale : " + (maxSize / 1024 / 1024) + " Mo");
        }

        String original = StringUtils
                .cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = getExtension(original).toLowerCase();

        if (!allowedExt.contains(extension)) {
            throw new FileStorageException("Type de fichier non autorisé. Extension acceptée : " + allowedExt);
        }

        // Validation par signature magique (vraie détection du contenu)
        if (requireImage) {
            validateImageMagic(file);
        } else {
            validatePdfMagic(file);
        }

        try {
            Path targetDir = uploadRoot.resolve(subDir).normalize();
            Files.createDirectories(targetDir);
            String storedName = UUID.randomUUID() + "." + extension;
            Path target = targetDir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + subDir + "/" + storedName;
        } catch (IOException ex) {
            throw new FileStorageException("Erreur lors de l'enregistrement du fichier", ex);
        }
    }

    private void validateImageMagic(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = in.readNBytes(header, 0, header.length);
            if (read < 8) {
                throw new FileStorageException("Fichier image invalide");
            }
            boolean jpeg = (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
            boolean png = (header[0] & 0xFF) == 0x89
                    && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
            boolean gif = header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
            boolean webp = (header[0] & 0xFF) == 0x52
                    && (header[1] & 0xFF) == 0x49 && (header[2] & 0xFF) == 0x46
                    && (header[3] & 0xFF) == 0x46 && (header[4] & 0xFF) == 0x57;
            if (!jpeg && !png && !gif && !webp) {
                throw new FileStorageException("Le contenu du fichier n'est pas une image valide (JPEG/PNG/GIF/WEBP)");
            }
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de lire le fichier", ex);
        }
    }

    private void validatePdfMagic(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[5];
            int read = in.readNBytes(header, 0, header.length);
            if (read < 5 || !"%PDF-".equalsIgnoreCase(new String(header, java.nio.charset.StandardCharsets.US_ASCII))) {
                throw new FileStorageException("Le contenu du fichier n'est pas un PDF valide");
            }
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de lire le fichier", ex);
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot == -1) ? "" : filename.substring(dot + 1);
    }

    private long parseSize(String value) {
        String v = value.trim().toUpperCase();
        try {
            if (v.endsWith("MB")) {
                return Long.parseLong(v.replace("MB", "").trim()) * 1024 * 1024;
            }
            if (v.endsWith("KB")) {
                return Long.parseLong(v.replace("KB", "").trim()) * 1024;
            }
            if (v.endsWith("B")) {
                return Long.parseLong(v.replace("B", "").trim());
            }
            return Long.parseLong(v);
        } catch (NumberFormatException ex) {
            return 2L * 1024 * 1024;
        }
    }
}
