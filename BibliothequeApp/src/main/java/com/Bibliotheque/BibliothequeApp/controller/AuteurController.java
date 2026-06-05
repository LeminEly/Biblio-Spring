package com.Bibliotheque.BibliothequeApp.controller;

import com.Bibliotheque.BibliothequeApp.model.Auteur;
import com.Bibliotheque.BibliothequeApp.services.AuteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auteurs")
public class AuteurController {

    @Autowired
    private AuteurService auteurService;

    @GetMapping
    public List<Auteur> getAll() {
        return auteurService.findAll();
    }

    @GetMapping("/{id}")
    public Auteur getById(@PathVariable Long id) {
        return auteurService.findById(id);
    }

    @PostMapping
    public Auteur create(@RequestBody Auteur auteur) {
        return auteurService.create(auteur);
    }

    @PutMapping("/{id}")
    public Auteur update(@PathVariable Long id, @RequestBody Auteur auteurModifie) {
        return auteurService.update(id, auteurModifie);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        auteurService.delete(id);
    }
}
