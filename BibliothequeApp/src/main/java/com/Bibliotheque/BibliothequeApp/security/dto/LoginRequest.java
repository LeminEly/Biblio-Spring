package com.Bibliotheque.BibliothequeApp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête de connexion")
public class LoginRequest {

    @Schema(description = "Email de l'utilisateur", example = "admin@bibliotheque.com")
    private String email;

    @Schema(description = "Mot de passe", example = "admin123")
    private String motDePasse;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
