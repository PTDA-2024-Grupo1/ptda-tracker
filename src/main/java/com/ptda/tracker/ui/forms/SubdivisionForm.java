package com.ptda.tracker.ui.forms;

import com.ptda.tracker.models.dispute.Subdivision;
import com.ptda.tracker.models.tracker.Budget;
import com.ptda.tracker.models.tracker.BudgetAccess;
import com.ptda.tracker.models.tracker.Expense;
import com.ptda.tracker.models.user.User;
import com.ptda.tracker.services.tracker.BudgetAccessService;
import com.ptda.tracker.services.tracker.SubdivisionService;
import com.ptda.tracker.ui.MainFrame;
import com.ptda.tracker.ui.views.ExpenseDetailView;
import com.ptda.tracker.util.ScreenNames;
import com.ptda.tracker.util.UserSession;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubdivisionForm extends JPanel {
    private final MainFrame mainFrame;
    private final Expense expense;
    private final Budget budget;
    private final BudgetAccessService budgetAccessService;
    private final SubdivisionService subdivisionService;
    private final List<Subdivision> subdivisions;
    private final Runnable onBack;

    private JRadioButton equitableRadioButton, customRadioButton;
    private JPanel customDistributionPanel;
    private JLabel totalPercentageLabel;

    private static final String
            DISTRIBUTION_TYPE = "Distribution Type:",
            EQUITABLE = "Equitable",
            CUSTOM = "Custom",
            CUSTOM_DISTRIBUTION = "Custom Distribution",
            CANCEL = "Cancel",
            DISTRIBUTE = "Distribute",
            DISTRIBUTION_COMPLETE = "Distribution complete (100%).",
            DISTRIBUTION_SAVED = "Distribution saved. Remaining percentage to distribute: ",
            REMAINING_PERCENTAGE_DISTRIBUTED = "Remaining percentage distributed equitably. Each user received: ",
            NO_USERS_LEFT = "No users left to distribute the remaining percentage.",
            INVALID_PERCENTAGE = "Invalid percentage value.",
            PERCENTAGE_GREATER_THAN_ZERO = "Percentage must be greater than 0.",
            TOTAL_PERCENTAGE_EXCEED = "The total percentage must not exceed 100%. Remaining percentage: ",
            NO_USERS_SELECTED = "No users selected for equitable distribution.",
            DISTRIBUTION_ALREADY_COMPLETE = "The distribution is already complete (100%). No remaining percentage to distribute.",
            PERCENTAGE = "Percentage";

    public SubdivisionForm(MainFrame mainFrame, Expense expense, Budget budget, Runnable onBack) {
        this.mainFrame = mainFrame;
        this.expense = expense;
        this.budget = budget;
        this.budgetAccessService = mainFrame.getContext().getBean(BudgetAccessService.class);
        this.subdivisionService = mainFrame.getContext().getBean(SubdivisionService.class);
        this.subdivisions = subdivisionService.getAllByExpenseId(expense.getId());
        this.onBack = onBack;

        initUI();
        setListeners();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 15, 15, 15);

        // Distribution Type RadioButtons
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel distributionLabel = new JLabel(DISTRIBUTION_TYPE);
        distributionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(distributionLabel, gbc);

        gbc.gridx = 1;
        JPanel distributionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        equitableRadioButton = new JRadioButton(EQUITABLE);
        customRadioButton = new JRadioButton(CUSTOM);
        ButtonGroup distributionGroup = new ButtonGroup();
        distributionGroup.add(equitableRadioButton);
        distributionGroup.add(customRadioButton);
        distributionPanel.add(equitableRadioButton);
        distributionPanel.add(customRadioButton);
        formPanel.add(distributionPanel, gbc);

        // Custom Distribution Panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        customDistributionPanel = new JPanel();
        customDistributionPanel.setLayout(new BoxLayout(customDistributionPanel, BoxLayout.Y_AXIS));
        customDistributionPanel.setBorder(BorderFactory.createTitledBorder(CUSTOM_DISTRIBUTION));
        customDistributionPanel.setVisible(false);
        formPanel.add(customDistributionPanel, gbc);

        // Total Percentage Label
        gbc.gridy = 2;
        totalPercentageLabel = new JLabel("Total Percentage: 0%");
        totalPercentageLabel.setVisible(false); // Ensure the label is visible
        formPanel.add(totalPercentageLabel, gbc);

        // Buttons Panel
        gbc.gridy = 3;
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton cancelButton = new JButton(CANCEL);
        cancelButton.addActionListener(e -> mainFrame.showScreen(ScreenNames.EXPENSE_DETAIL_VIEW));
        buttonsPanel.add(cancelButton);

        JButton distributeButton = new JButton(DISTRIBUTE);
        distributeButton.addActionListener(e -> distributeExpenses());
        buttonsPanel.add(distributeButton);

        formPanel.add(buttonsPanel, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void setListeners() {
        customRadioButton.addActionListener(e -> {
            customDistributionPanel.setVisible(true);
            totalPercentageLabel.setVisible(true);
            addCustomDistributionRows();
        });
        equitableRadioButton.addActionListener(e -> {
            customDistributionPanel.setVisible(false);
            totalPercentageLabel.setVisible(false);
        });
    }

    private void distributeExpenses() {
        if (equitableRadioButton.isSelected()) {
            distributeEquitably();
        } else if (customRadioButton.isSelected()) {
            distributeCustom();
        }
    }

    private BudgetAccess[] getUsers(Budget budget, BudgetAccessService budgetAccessService) {
        List<BudgetAccess> accesses = budgetAccessService.getAllByBudgetId(budget.getId());
        return accesses.toArray(new BudgetAccess[0]);
    }

    private void distributeEquitably() {
        Expense selectedExpense = expense;
        List<Subdivision> existingSubdivisions = subdivisionService.getAllByExpenseId(selectedExpense.getId());
        double totalPercentage = existingSubdivisions.stream().mapToDouble(Subdivision::getPercentage).sum();
        double remainingPercentage = 100 - totalPercentage;
        User currentUser = UserSession.getInstance().getUser();

        if (remainingPercentage <= 0) {
            JOptionPane.showMessageDialog(this, DISTRIBUTION_ALREADY_COMPLETE);
            return;
        }

        // Create a checklist for users
        List<User> includedUsers = existingSubdivisions.stream()
                .map(Subdivision::getUser)
                .collect(Collectors.toList());

        List<BudgetAccess> usersToDistribute = new ArrayList<>();
        for (BudgetAccess access : getUsers(budget, budgetAccessService)) {
            if (!includedUsers.contains(access.getUser())) {
                usersToDistribute.add(access);
            }
        }

        if (usersToDistribute.isEmpty()) {
            JOptionPane.showMessageDialog(this, NO_USERS_LEFT);
            return;
        }

        // Show checklist dialog
        List<BudgetAccess> selectedUsers = showUserChecklistDialog(usersToDistribute);
        if (selectedUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, NO_USERS_SELECTED);
            return;
        }

        // Calculate the equitable percentage for each selected user
        double equalPercentage = remainingPercentage / selectedUsers.size();

        for (BudgetAccess userAccess : selectedUsers) {
            User user = userAccess.getUser();
            double amount = expense.getAmount() * (equalPercentage / 100);

            Subdivision subdivision = Subdivision.builder()
                    .expense(expense)
                    .user(user)
                    .amount(amount)
                    .percentage(equalPercentage)
                    .createdBy(currentUser)
                    .build();

            subdivisions.add(subdivision);
            subdivisionService.create(subdivision);
        }

        JOptionPane.showMessageDialog(this, REMAINING_PERCENTAGE_DISTRIBUTED + equalPercentage + "%.");

        if (onBack != null) {
            onBack.run();
        } else {
            mainFrame.registerAndShowScreen(ScreenNames.EXPENSE_DETAIL_VIEW, new ExpenseDetailView(mainFrame, expense, ScreenNames.EXPENSE_DETAIL_VIEW, () -> {}));
        }
    }

    private List<BudgetAccess> showUserChecklistDialog(List<BudgetAccess> usersToDistribute) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> checkBoxes = new ArrayList<>();
        for (BudgetAccess userAccess : usersToDistribute) {
            JCheckBox checkBox = new JCheckBox(userAccess.getUser().getName());
            checkBoxes.add(checkBox);
            panel.add(checkBox);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Users for Equitable Distribution", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return checkBoxes.stream()
                    .filter(JCheckBox::isSelected)
                    .map(checkBox -> usersToDistribute.get(checkBoxes.indexOf(checkBox)))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void distributeCustom() {
        Expense selectedExpense = expense;
        List<Subdivision> existingSubdivisions = subdivisionService.getAllByExpenseId(selectedExpense.getId());
        User currentUser = UserSession.getInstance().getUser();

        double newTotalPercentage = 0;
        List<Subdivision> subdivisionsToRemove = new ArrayList<>();

        for (Component component : customDistributionPanel.getComponents()) {
            if (component instanceof JPanel panel) {
                JLabel userLabel = (JLabel) panel.getComponent(0);
                JTextField percentageField = (JTextField) panel.getComponent(2);

                String percentageText = percentageField.getText().trim();
                String userName = userLabel.getText().replace(":", "").trim();
                BudgetAccess selectedUser = getUserByName(userName);
                if (selectedUser == null) continue;

                if (percentageText.isEmpty()) {
                    // Remove subdivision if the field is left blank
                    existingSubdivisions.stream()
                            .filter(sub -> sub.getUser().equals(selectedUser.getUser()))
                            .findFirst()
                            .ifPresent(sub -> {
                                subdivisionsToRemove.add(sub);
                            });
                    continue;
                }

                double percentage;
                try {
                    percentage = Double.parseDouble(percentageText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, INVALID_PERCENTAGE);
                    return;
                }

                if (percentage <= 0) {
                    JOptionPane.showMessageDialog(this, PERCENTAGE_GREATER_THAN_ZERO);
                    return;
                }

                newTotalPercentage += percentage;
                if (newTotalPercentage > 100) {
                    JOptionPane.showMessageDialog(this, TOTAL_PERCENTAGE_EXCEED + (100 - newTotalPercentage) + "%.");
                    return;
                }

                double amount = expense.getAmount() * (percentage / 100);
                Subdivision subdivision = existingSubdivisions.stream()
                        .filter(sub -> sub.getUser().equals(selectedUser.getUser()))
                        .findFirst()
                        .orElse(Subdivision.builder()
                                .expense(expense)
                                .user(selectedUser.getUser())
                                .createdBy(currentUser)
                                .build());

                subdivision.setPercentage(percentage);
                subdivision.setAmount(amount);

                if (!existingSubdivisions.contains(subdivision)) {
                    subdivisions.add(subdivision);
                    subdivisionService.create(subdivision);
                } else {
                    subdivisionService.update(subdivision);
                }
            }
        }

        // Remove subdivisions for users with blank fields
        for (Subdivision sub : subdivisionsToRemove) {
            subdivisions.remove(sub);
            subdivisionService.delete(sub.getId());
        }

        double remainingAfterDistribution = 100 - newTotalPercentage;
        if (remainingAfterDistribution > 0) {
            JOptionPane.showMessageDialog(this, DISTRIBUTION_SAVED + remainingAfterDistribution + "%.");
        } else {
            JOptionPane.showMessageDialog(this, DISTRIBUTION_COMPLETE);
        }

        if (onBack != null) {
            onBack.run();
        }
    }

    private BudgetAccess getUserByName(String userName) {
        for (BudgetAccess access : getUsers(budget, budgetAccessService)) {
            if (access.getUser().getName().equals(userName)) {
                return access;
            }
        }
        return null;
    }

    private void addCustomDistributionRows() {
        customDistributionPanel.removeAll();
        BudgetAccess[] users = getUsers(budget, budgetAccessService);
        List<User> existingUsers = subdivisions.stream()
                .map(Subdivision::getUser)
                .collect(Collectors.toList());

        // Adicionar as subdivisões existentes
        for (Subdivision subdivision : subdivisions) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel userLabel = new JLabel(subdivision.getUser().getName() + ":");
            JTextField percentageField = new JTextField(String.valueOf(subdivision.getPercentage()), 5); // Valor já definido
            percentageField.getDocument().addDocumentListener(new PercentageFieldListener());
            rowPanel.add(userLabel);
            rowPanel.add(new JLabel(PERCENTAGE + ":"));
            rowPanel.add(percentageField);
            customDistributionPanel.add(rowPanel);
        }

        // Adicionar usuários que ainda não têm uma subdivisão
        for (BudgetAccess userAccess : users) {
            if (!existingUsers.contains(userAccess.getUser())) {
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel userLabel = new JLabel(userAccess.getUser().getName() + ":");
                JTextField percentageField = new JTextField("", 5); // Valor inicial vazio
                percentageField.getDocument().addDocumentListener(new PercentageFieldListener());
                rowPanel.add(userLabel);
                rowPanel.add(new JLabel(PERCENTAGE + ":"));
                rowPanel.add(percentageField);
                customDistributionPanel.add(rowPanel);
            }
        }

        customDistributionPanel.revalidate();
        customDistributionPanel.repaint();
    }


    private class PercentageFieldListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateTotalPercentage();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateTotalPercentage();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateTotalPercentage();
        }
    }

    private void updateTotalPercentage() {
        double totalPercentage = 0;
        for (Component component : customDistributionPanel.getComponents()) {
            if (component instanceof JPanel panel) {
                JTextField percentageField = (JTextField) panel.getComponent(2);
                String percentageText = percentageField.getText().trim();
                if (!percentageText.isEmpty()) {
                    try {
                        totalPercentage += Double.parseDouble(percentageText);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        totalPercentageLabel.setText("Total Percentage: " + totalPercentage + "%");
    }
}