package com.Bibliotheque.BibliothequeApp.services;

import com.Bibliotheque.BibliothequeApp.exception.ResourceNotFoundException;
import com.Bibliotheque.BibliothequeApp.model.Auteur;
import com.Bibliotheque.BibliothequeApp.model.Livre;
import com.Bibliotheque.BibliothequeApp.repository.AuteurRepository;
import com.Bibliotheque.BibliothequeApp.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivreService {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AuteurRepository auteurRepository;

    public List<Livre> findAll() {
        return livreRepository.findAll();
    }

    public Livre findById(Long id) {
        return livreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Livre", id));
    }

    public Livre create(Livre livre) {
        livre.setAuteur(resolveAuteur(livre.getAuteur()));
        return livreRepository.save(livre);
    }

    public Livre update(Long id, Livre livreModifie) {
        Livre livre = findById(id);
        livre.setTitre(livreModifie.getTitre());
        livre.setIsbn(livreModifie.getIsbn());
        livre.setAnneePublication(livreModifie.getAnneePublication());
        livre.setAuteur(resolveAuteur(livreModifie.getAuteur()));
        return livreRepository.save(livre);
    }

    public void delete(Long id) {
        if (!livreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livre", id);
        }
        livreRepository.deleteById(id);
    }

    private Auteur resolveAuteur(Auteur auteur) {
        if (auteur == null || auteur.getId() == null) {
            return null;
        }
        return auteurRepository.findById(auteur.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Auteur", auteur.getId()));
    }
}
