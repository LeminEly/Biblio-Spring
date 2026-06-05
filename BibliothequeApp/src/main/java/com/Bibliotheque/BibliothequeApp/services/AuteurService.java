package com.Bibliotheque.BibliothequeApp.services;

import com.Bibliotheque.BibliothequeApp.exception.ResourceNotFoundException;
import com.Bibliotheque.BibliothequeApp.model.Auteur;
import com.Bibliotheque.BibliothequeApp.repository.AuteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuteurService {

    @Autowired
    private AuteurRepository auteurRepository;

    public List<Auteur> findAll() {
        return auteurRepository.findAll();
    }

    public Auteur findById(Long id) {
        return auteurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Auteur", id));
    }

    public Auteur create(Auteur auteur) {
        return auteurRepository.save(auteur);
    }

    public Auteur update(Long id, Auteur auteurModifie) {
        Auteur auteur = findById(id);
        auteur.setNom(auteurModifie.getNom());
        return auteurRepository.save(auteur);
    }

    public void delete(Long id) {
        if (!auteurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Auteur", id);
        }
        auteurRepository.deleteById(id);
    }
}
