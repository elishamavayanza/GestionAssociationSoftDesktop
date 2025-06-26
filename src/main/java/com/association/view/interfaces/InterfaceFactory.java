package com.association.view.interfaces;

import com.association.security.model.Role;
import com.association.security.model.Utilisateur;

public class InterfaceFactory {
    public static RoleInterface createInterface(Utilisateur utilisateur) {
        if (utilisateur.hasRole(Role.ADMIN)) {
            return new AdminInterface(utilisateur);
        } else if (utilisateur.hasRole(Role.GESTIONNAIRE)) {
            return new GestionnaireInterface(utilisateur);
        } else if (utilisateur.hasRole(Role.MEMBRE)) {
            return new MembreInterface(utilisateur);
        } else {
            return new VisiteurInterface(utilisateur);
        }
    }
}