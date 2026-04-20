package com.Bibliotheque.BibliothequeApp.controller;

import com.Bibliotheque.BibliothequeApp.model.Livre;
import com.Bibliotheque.BibliothequeApp.services.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livres")
public class LivreController {

    @Autowired
    private LivreRepository livreRepository;

    @GetMapping
    public List<Livre> getAll() {
        return livreRepository.findAll();
    }

    @GetMapping("/{id}")
    public Livre getById(@PathVariable Long id) {
        return livreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));
    }

    @PostMapping
    public Livre create(@RequestBody Livre livre) {
        return livreRepository.save(livre);
    }

    @PutMapping("/{id}")
    public Livre update(@PathVariable Long id, @RequestBody Livre livreModifie) {
        Livre livre = livreRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));
        livre.setTitre(livreModifie.getTitre());
        livre.setIsbn(livreModifie.getIsbn());
        livre.setAnneePublication(livreModifie.getAnneePublication());
        livre.setAuteur(livreModifie.getAuteur());
        return livreRepository.save(livre);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        livreRepository.deleteById(id);
    }
}