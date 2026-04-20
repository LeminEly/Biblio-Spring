package com.Bibliotheque.BibliothequeApp.controller;

import com.Bibliotheque.BibliothequeApp.model.Auteur;
import com.Bibliotheque.BibliothequeApp.services.AuteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auteurs")
public class AuteurController {

    @Autowired
    private AuteurRepository auteurRepository;

    @GetMapping
    public List<Auteur> getAll() {
        return auteurRepository.findAll();
    }

    @GetMapping("/{id}")
    public Auteur getById(@PathVariable Long id) {
        return auteurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auteur non trouvé"));
    }

    @PostMapping
    public Auteur create(@RequestBody Auteur auteur) {
        return auteurRepository.save(auteur);
    }

    @PutMapping("/{id}")
    public Auteur update(@PathVariable Long id, @RequestBody Auteur auteurModifie) {
        Auteur auteur = auteurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auteur non trouvé"));
        auteur.setNom(auteurModifie.getNom());
        return auteurRepository.save(auteur);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        auteurRepository.deleteById(id);
    }
}