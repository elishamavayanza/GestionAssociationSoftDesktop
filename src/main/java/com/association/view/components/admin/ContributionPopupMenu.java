package com.association.view.components.admin;

import com.association.manager.ContributionManager;
import com.association.model.enums.TypeContribution;
import com.association.model.transaction.Contribution;
import com.association.util.utils.CustomDialog;
import com.association.util.utils.ExchangeRateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class ContributionPopupMenu extends JPopupMenu {
    private static final String CURRENCY_CDF = "CDF";
    private static final String CURRENCY_USD = "USD";

    private final JTextField contributionField;
    private final ContributionManager contributionManager;
    private final LocalDate contributionDate;
    private final String currentCurrency;
    private final WeeklyCalendarPanel parentPanel;

    public ContributionPopupMenu(JTextField contributionField,
                                 ContributionManager contributionManager,
                                 LocalDate contributionDate,
                                 String currentCurrency,
                                 WeeklyCalendarPanel parentPanel) {
        this.contributionField = contributionField;
        this.contributionManager = contributionManager;
        this.contributionDate = contributionDate;
        this.currentCurrency = currentCurrency;
        this.parentPanel = parentPanel;

        initMenuItems();
    }

    private void initMenuItems() {
        JMenuItem modifyItem = new JMenuItem("Modifier");
        modifyItem.addActionListener(this::modifyContribution);
        this.add(modifyItem);

        JMenuItem deleteItem = new JMenuItem("Supprimer");
        deleteItem.addActionListener(this::deleteContribution);
        this.add(deleteItem);
    }

    private void modifyContribution(ActionEvent e) {
        String currentValue = contributionField.getText();
        if (currentValue == null || currentValue.isEmpty()) return;

        String newValue = CustomDialog.showInputDialog(
                parentPanel,
                "Modifier la contribution:",
                "Modification"
        );

        if (newValue != null && !newValue.isEmpty()) {
            try {
                BigDecimal newAmount = new BigDecimal(newValue);
                if (currentCurrency.equals(CURRENCY_USD)) {
                    newAmount = ExchangeRateUtil.convert(newAmount, CURRENCY_USD, CURRENCY_CDF);
                }

                List<Contribution> contributions = contributionManager.getContributionsBetweenDates(
                        Date.valueOf(contributionDate),
                        Date.valueOf(contributionDate)
                );

                if (contributions != null) {
                    for (Contribution contrib : contributions) {
                        BigDecimal contribAmount = contrib.getMontant();
                        String contribAmountStr = currentCurrency.equals(CURRENCY_USD)
                                ? ExchangeRateUtil.convert(contribAmount, CURRENCY_CDF, CURRENCY_USD).toString()
                                : contribAmount.toString();

                        if (contribAmountStr.equals(currentValue)) {
                            contrib.setMontant(newAmount);
                            if (contrib.getTypeContribution() == null) {
                                contrib.setTypeContribution(TypeContribution.MENSUEL);
                            }

                            if (contributionManager.update(contrib)) {
                                contributionField.setText(newValue);
                                parentPanel.updateCalendar();
                                showSuccessMessage("Contribution modifiée avec succès!");
                            } else {
                                showErrorMessage("Échec de la modification");
                            }
                            return;
                        }
                    }
                }
                showErrorMessage("Aucune contribution correspondante trouvée");
            } catch (NumberFormatException ex) {
                showErrorMessage("Veuillez entrer un montant valide");
            } catch (Exception ex) {
                showErrorMessage("Erreur lors de la modification: " + ex.getMessage());
            }
        }
    }

    private void deleteContribution(ActionEvent e) {
        String currentValue = contributionField.getText();
        if (currentValue == null || currentValue.isEmpty()) return;

        int confirm = CustomDialog.showConfirmDialog(
                parentPanel,
                "Êtes-vous sûr de vouloir supprimer cette contribution?",
                "Confirmation"
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                List<Contribution> contributions = contributionManager.getContributionsBetweenDates(
                        Date.valueOf(contributionDate),
                        Date.valueOf(contributionDate)
                );

                if (contributions != null) {
                    for (Contribution contrib : contributions) {
                        BigDecimal contribAmount = contrib.getMontant();
                        String contribAmountStr = currentCurrency.equals(CURRENCY_USD)
                                ? ExchangeRateUtil.convert(contribAmount, CURRENCY_CDF, CURRENCY_USD).toString()
                                : contribAmount.toString();

                        if (contribAmountStr.equals(currentValue)) {
                            if (contributionManager.delete(contrib.getId())) {
                                contributionField.setText("");
                                contributionField.setToolTipText(null);
                                parentPanel.updateCalendar();
                                showSuccessMessage("Contribution supprimée avec succès!");
                            } else {
                                showErrorMessage("Échec de la suppression");
                            }
                            return;
                        }
                    }
                }
                showErrorMessage("Aucune contribution correspondante trouvée");
            } catch (Exception ex) {
                showErrorMessage("Erreur lors de la suppression: " + ex.getMessage());
            }
        }
    }

    private void showSuccessMessage(String message) {
        CustomDialog.showSuccessDialog(
                parentPanel,
                message,
                "Succès"
        );
    }

    private void showErrorMessage(String message) {
        CustomDialog.showErrorDialog(
                parentPanel,
                message,
                "Erreur"
        );
    }
}