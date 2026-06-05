package com.Bibliotheque.BibliothequeApp.controller;

import com.Bibliotheque.BibliothequeApp.model.Auteur;
import com.Bibliotheque.BibliothequeApp.services.AuteurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auteurs")
@Tag(name = "Auteurs", description = "CRUD des auteurs")
public class AuteurController {

    @Autowired
    private AuteurService auteurService;

    @GetMapping
    @Operation(summary = "Liste des auteurs", description = "Retourne tous les auteurs")
    public List<Auteur> getAll() {
        return auteurService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Auteur par ID", description = "Retourne un auteur par son identifiant")
    public Auteur getById(@PathVariable Long id) {
        return auteurService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Créer un auteur", description = "Crée un nouvel auteur (Admin seulement)")
    public Auteur create(@RequestBody Auteur auteur) {
        return auteurService.create(auteur);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un auteur", description = "Met à jour un auteur existant (Admin seulement)")
    public Auteur update(@PathVariable Long id, @RequestBody Auteur auteurModifie) {
        return auteurService.update(id, auteurModifie);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un auteur", description = "Supprime un auteur par son identifiant (Admin seulement)")
    public void delete(@PathVariable Long id) {
        auteurService.delete(id);
    }
}
