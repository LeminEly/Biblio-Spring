package com.Bibliotheque.BibliothequeApp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Réponse d'authentification")
public class AuthResponse {

    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Type du token", example = "Bearer")
    private final String type = "Bearer";

    @Schema(description = "Email de l'utilisateur", example = "admin@bibliotheque.com")
    private String email;

    @Schema(description = "Rôle de l'utilisateur", example = "ADMIN")
    private String role;

    public AuthResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getType() { return type; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
