package com.association.view.components.admin;

import com.association.dao.DAOFactory;
import com.association.dao.MembreDao;
import com.association.model.Membre;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ListeMemberbyName extends JPanel {
    private final JList<String> membreList;
    private final DefaultListModel<String> listModel;

    public ListeMemberbyName() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Membres existants"));

        listModel = new DefaultListModel<>();
        membreList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(membreList);
        add(scrollPane, BorderLayout.CENTER);

        chargerMembres();
    }

    private void chargerMembres() {
        MembreDao membreDao = DAOFactory.getInstance(MembreDao.class);
        List<Membre> membres = membreDao.findAll();

        listModel.clear();
        for (Membre membre : membres) {
            listModel.addElement(membre.getNom());
        }
    }
}
