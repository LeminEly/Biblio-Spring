package com.Bibliotheque.BibliothequeApp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête d'inscription")
public class RegisterRequest {

    @Schema(description = "Nom d'utilisateur", example = "nouvelutilisateur")
    private String nom;

    @Schema(description = "Email", example = "user@bibliotheque.com")
    private String email;

    @Schema(description = "Mot de passe", example = "password123")
    private String motDePasse;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
