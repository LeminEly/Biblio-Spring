package com.Bibliotheque.BibliothequeApp.config;

import com.Bibliotheque.BibliothequeApp.model.Utilisateur;
import com.Bibliotheque.BibliothequeApp.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UtilisateurRepository utilisateurRepository,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!utilisateurRepository.existsByEmail("admin@bibliotheque.com")) {
            Utilisateur admin = new Utilisateur(
                    "admin",
                    "admin@bibliotheque.com",
                    passwordEncoder.encode("admin123"),
                    "ADMIN"
            );
            utilisateurRepository.save(admin);
        }

        if (!utilisateurRepository.existsByEmail("user@bibliotheque.com")) {
            Utilisateur user = new Utilisateur(
                    "user",
                    "user@bibliotheque.com",
                    passwordEncoder.encode("user123"),
                    "USER"
            );
            utilisateurRepository.save(user);
        }
    }
}
