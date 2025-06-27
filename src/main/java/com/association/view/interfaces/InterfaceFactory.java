package com.association.view.interfaces;

import com.association.model.enums.UserRole;
import com.association.model.access.Utilisateur;

public class InterfaceFactory {
    public static RoleInterface createInterface(Utilisateur utilisateur) {
        if (utilisateur.hasRole(UserRole.ADMIN)) {
            return new AdminInterface(utilisateur);
        } else if (utilisateur.hasRole(UserRole.GESTIONNAIRE)) {
            return new GestionnaireInterface(utilisateur);
        } else if (utilisateur.hasRole(UserRole.MEMBRE)) {
            return new MembreInterface(utilisateur);
        } else {
            return new VisiteurInterface(utilisateur);
        }
    }
}