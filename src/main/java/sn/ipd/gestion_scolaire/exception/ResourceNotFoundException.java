package sn.ipd.gestion_scolaire.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " introuvable avec l'identifiant : " + id);
    }
}
