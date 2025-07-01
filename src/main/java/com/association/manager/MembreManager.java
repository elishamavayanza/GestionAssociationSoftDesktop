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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MembreManager extends BaseManager<Membre> {
    private final MembreDao membreDao;
    private final FileStorageService fileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(MembreManager.class);


    public MembreManager(MembreDao membreDao, FileStorageService fileStorageService) {
        super(membreDao);
        this.membreDao = membreDao;
        this.fileStorageService = fileStorageService;
    }

    public boolean ajouterMembre(String nom, String contact, byte[] photo, Date dateInscription, StatutMembre statut) {
        logger.debug("Tentative d'ajout d'un membre avec photo: {} bytes", photo != null ? photo.length : 0);

        Membre membre = new Membre();
        membre.setNom(nom);
        membre.setContact(contact);
        membre.setDateInscription(dateInscription);
        membre.setStatut(statut);

        if (photo != null && photo.length > 0) {
            String subDir = "membres/photos";
            logger.debug("Tentative de stockage dans: {}", subDir);

            String photoPath = fileStorageService.storeFile(photo, subDir);

            if (photoPath == null) { // Maintenant on vérifie null plutôt que vide
                logger.error("Échec critique du stockage de la photo");
                return false;
            }

            membre.setPhoto(photoPath);
            logger.debug("Photo enregistrée avec chemin: {}", photoPath);
        }

        try {
            boolean created = membreDao.create(membre);
            logger.debug("Résultat création membre: {}", created);

            if (!created && membre.getPhoto() != null) {
                logger.warn("Suppression de la photo suite à échec de création");
                fileStorageService.deleteFile(membre.getPhoto());
            }
            return created;
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout du membre", e);
            if (membre.getPhoto() != null) {
                fileStorageService.deleteFile(membre.getPhoto());
            }
            return false;
        }
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