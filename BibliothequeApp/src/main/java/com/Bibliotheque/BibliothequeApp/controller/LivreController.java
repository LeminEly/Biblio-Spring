package com.Bibliotheque.BibliothequeApp.controller;

import com.Bibliotheque.BibliothequeApp.model.Livre;
import com.Bibliotheque.BibliothequeApp.services.LivreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livres")
public class LivreController {

    @Autowired
    private LivreService livreService;

    @GetMapping
    public List<Livre> getAll() {
        return livreService.findAll();
    }

    @GetMapping("/{id}")
    public Livre getById(@PathVariable Long id) {
        return livreService.findById(id);
    }

    @PostMapping
    public Livre create(@RequestBody Livre livre) {
        return livreService.create(livre);
    }

    @PutMapping("/{id}")
    public Livre update(@PathVariable Long id, @RequestBody Livre livreModifie) {
        return livreService.update(id, livreModifie);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        livreService.delete(id);
    }
}
