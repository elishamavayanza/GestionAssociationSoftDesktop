package com.association.dao;

import com.association.security.model.Utilisateur;
import java.util.Optional;

public interface UtilisateurDao extends GenericDao<Utilisateur> {
    boolean updateAvatar(Long userId, String avatarPath);
    Optional<Utilisateur> findByEmail(String email);
}