package com.association.view.interfaces;

import javax.swing.JFrame;

public interface RoleInterface {
    JFrame createInterface();
    String getRoleName();
    boolean hasAccessToFeature(String featureName);
}