package com.Bibliotheque.BibliothequeApp.repository;

import com.Bibliotheque.BibliothequeApp.model.Auteur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuteurRepository extends JpaRepository<Auteur, Long> {
}
