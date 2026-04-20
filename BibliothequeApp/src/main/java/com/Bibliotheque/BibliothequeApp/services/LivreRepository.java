package com.Bibliotheque.BibliothequeApp.services;

import com.Bibliotheque.BibliothequeApp.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivreRepository extends JpaRepository<Livre, Long> {
}