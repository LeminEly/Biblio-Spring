package com.Bibliotheque.BibliothequeApp.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Auteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @OneToMany(mappedBy = "auteur", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Livre> livres;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public List<Livre> getLivres() { return livres; }
    public void setLivres(List<Livre> livres) { this.livres = livres; }
}