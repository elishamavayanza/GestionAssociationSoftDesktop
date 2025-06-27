package com.association.dao;

import com.association.model.access.Utilisateur;
import java.util.Optional;

public interface UtilisateurDao extends GenericDao<Utilisateur> {
    boolean updateAvatar(Long userId, String avatarPath);
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByUsername(String username); // Ajouté
    boolean update(Utilisateur utilisateur); // Pour mettre à jour les tentatives de connexion
    String getAvatarPath(Long userId);
}