package com.Bibliotheque.BibliothequeApp.controller;

import com.Bibliotheque.BibliothequeApp.model.Livre;
import com.Bibliotheque.BibliothequeApp.services.LivreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livres")
@Tag(name = "Livres", description = "CRUD des livres")
public class LivreController {

    @Autowired
    private LivreService livreService;

    @GetMapping
    @Operation(summary = "Liste des livres", description = "Retourne tous les livres")
    public List<Livre> getAll() {
        return livreService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Livre par ID", description = "Retourne un livre par son identifiant")
    public Livre getById(@PathVariable Long id) {
        return livreService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Créer un livre", description = "Crée un nouveau livre (Admin seulement)")
    public Livre create(@RequestBody Livre livre) {
        return livreService.create(livre);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un livre", description = "Met à jour un livre existant (Admin seulement)")
    public Livre update(@PathVariable Long id, @RequestBody Livre livreModifie) {
        return livreService.update(id, livreModifie);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un livre", description = "Supprime un livre par son identifiant (Admin seulement)")
    public void delete(@PathVariable Long id) {
        livreService.delete(id);
    }
}
