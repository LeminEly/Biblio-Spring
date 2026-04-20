package com.Bibliotheque.BibliothequeApp.model;

import jakarta.persistence.*;


@Entity
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    
    private String titre;

    private String isbn;

    private int anneePublication;

    @ManyToOne
    @JoinColumn(name = "auteur_id")
    private Auteur auteur;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getAnneePublication() { return anneePublication; }
    public void setAnneePublication(int anneePublication) { this.anneePublication = anneePublication; }

    public Auteur getAuteur() { return auteur; }
    public void setAuteur(Auteur auteur) { this.auteur = auteur; }
}