package com.association.util.utils;

import com.association.security.model.Utilisateur;
import com.association.security.model.Role;
import com.association.view.interfaces.InterfaceFactory;
import com.association.view.interfaces.RoleInterface;

public class PermissionUtil {
    public static boolean checkPermission(Utilisateur utilisateur, String featureName) {
        RoleInterface roleInterface = InterfaceFactory.createInterface(utilisateur);
        return roleInterface.hasAccessToFeature(featureName);
    }
}