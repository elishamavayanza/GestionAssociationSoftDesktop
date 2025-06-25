package com.association.model.transaction;

import java.math.BigDecimal;
import java.util.Date;
import com.association.model.Membre;
import com.association.model.enums.TypeContribution;

public class Contribution extends Transaction {
    private TypeContribution typeContribution;

    public Contribution() {
        super();
    }

    public Contribution(Long id, Date dateCreation, Membre membre, Date dateTransaction,
                        BigDecimal montant, String description, TypeContribution typeContribution) {
        super(id, dateCreation, membre, dateTransaction, montant, description);
        this.typeContribution = typeContribution;
    }

    // Getters and Setters
    public TypeContribution getTypeContribution() {
        return typeContribution;
    }

    public void setTypeContribution(TypeContribution typeContribution) {
        this.typeContribution = typeContribution;
    }
}