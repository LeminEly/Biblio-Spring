package com.Bibliotheque.BibliothequeApp.security.controller;

import com.Bibliotheque.BibliothequeApp.model.Utilisateur;
import com.Bibliotheque.BibliothequeApp.repository.UtilisateurRepository;
import com.Bibliotheque.BibliothequeApp.security.dto.AuthResponse;
import com.Bibliotheque.BibliothequeApp.security.dto.LoginRequest;
import com.Bibliotheque.BibliothequeApp.security.dto.RegisterRequest;
import com.Bibliotheque.BibliothequeApp.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints d'inscription et de connexion")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UtilisateurRepository utilisateurRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token JWT")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getMotDePasse()));

        Utilisateur user = (Utilisateur) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);

        return ResponseEntity.ok(
                new AuthResponse(token, user.getEmail(), user.getRole()));
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Crée un nouvel utilisateur avec le rôle USER")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email déjà utilisé"));
        }
        if (utilisateurRepository.existsByNom(request.getNom())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nom d'utilisateur déjà utilisé"));
        }

        Utilisateur user = new Utilisateur(
                request.getNom(),
                request.getEmail(),
                passwordEncoder.encode(request.getMotDePasse()),
                "USER"
        );

        utilisateurRepository.save(user);
        String token = jwtTokenProvider.generateToken(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getEmail(), user.getRole()));
    }
}
