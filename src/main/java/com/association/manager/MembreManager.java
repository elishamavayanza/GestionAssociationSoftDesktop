package com.association.manager;

import com.association.dao.MembreDao;
import com.association.model.Membre;
import com.association.model.enums.StatutMembre;
import com.association.manager.dto.MembreSearchCriteria;
import com.association.util.file.FileStorageService;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MembreManager extends BaseManager<Membre> {
    private final MembreDao membreDao;
    private final FileStorageService fileStorageService;

    public MembreManager(MembreDao membreDao, FileStorageService fileStorageService) {
        super(membreDao);
        this.membreDao = membreDao;
        this.fileStorageService = fileStorageService;
    }

    public boolean ajouterMembre(String nom, String contact, byte[] photo, Date dateInscription) {
        Membre membre = new Membre();
        membre.setNom(nom);
        membre.setContact(contact);
        membre.setDateInscription(dateInscription);

        if (photo != null) {
            String photoPath = fileStorageService.storeFile(photo, "membres/" + membre.getId() + "/photo");
            membre.setPhoto(photoPath);
        }

        return create(membre);
    }

    public boolean modifierMembre(Long id, String nom, String contact) {
        return membreDao.findById(id).map(membre -> {
            membre.setNom(nom);
            membre.setContact(contact);
            return update(membre);
        }).orElse(false);
    }

    public List<Membre> findByNom(String nom) {
        return membreDao.findByNom(nom);
    }

    public List<Membre> findByDateInscription(Date date) {
        return membreDao.findByDateInscription(date);
    }

    public boolean modifierPhotoMembre(Long membreId, byte[] photoFile) {
        String photoPath = fileStorageService.storeFile(photoFile, "membres/" + membreId + "/photo");
        return membreDao.updatePhoto(membreId, photoPath);
    }

    public InputStream getPhotoMembre(Long membreId) {
        return membreDao.findById(membreId)
                .map(Membre::getPhoto)
                .map(fileStorageService::loadFile)
                .orElse(null);
    }

    public Map<String, Object> getMembreStats() {
        // Implémentation des statistiques
        return Map.of(
                "total", membreDao.count(),
                "actifs", membreDao.countByStatut(StatutMembre.ACTIF),
                "inactifs", membreDao.countByStatut(StatutMembre.INACTIF),
                "suspendus", membreDao.countByStatut(StatutMembre.SUSPENDU)
        );
    }

    public List<Membre> searchMembres(MembreSearchCriteria criteria) {
        // Implémentation de la recherche avancée
        return membreDao.findByNom(criteria.getNom());
    }
}