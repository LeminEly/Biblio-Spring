package com.Bibliotheque.BibliothequeApp.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " non trouvé avec l'id : " + id);
    }
}
