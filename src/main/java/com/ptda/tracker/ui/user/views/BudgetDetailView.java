package com.ptda.tracker.ui.user.views;

import com.ptda.tracker.models.tracker.Budget;
import com.ptda.tracker.models.tracker.BudgetAccess;
import com.ptda.tracker.models.tracker.BudgetAccessLevel;
import com.ptda.tracker.models.tracker.Expense;
import com.ptda.tracker.models.user.User;
import com.ptda.tracker.services.tracker.BudgetAccessService;
import com.ptda.tracker.services.tracker.BudgetService;
import com.ptda.tracker.services.tracker.ExpenseService;
import com.ptda.tracker.ui.MainFrame;
import com.ptda.tracker.ui.user.dialogs.ParticipantsDialog;
import com.ptda.tracker.ui.user.forms.*;
import com.ptda.tracker.ui.user.screens.ExpensesImportScreen;
import com.ptda.tracker.util.ExpensesImportSharedData;
import com.ptda.tracker.util.LocaleManager;
import com.ptda.tracker.util.ScreenNames;
import com.ptda.tracker.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetDetailView extends JPanel {
    private final MainFrame mainFrame;
    private final BudgetAccessService budgetAccessService;
    private final User user = UserSession.getInstance().getUser();
    private final Budget budget;
    private final List<Expense> expenses;
    private final BudgetAccess budgetAccess;
    private final Runnable onBack;

    public BudgetDetailView(MainFrame mainFrame, Budget budget, Runnable onBack) {
        this.mainFrame = mainFrame;
        budgetAccessService = mainFrame.getContext().getBean(BudgetAccessService.class);
        Optional<BudgetAccess> optionalBudgetAccess = budgetAccessService.getAccessByBudgetIdAndUserId(budget.getId(), user.getId());
        if (optionalBudgetAccess.isPresent()) {
            budgetAccess = optionalBudgetAccess.get();
        } else {
            throw new RuntimeException("Budget access not found for user " + user.getId());
        }
        this.budget = budget;
        expenses = mainFrame.getContext().getBean(ExpenseService.class).getAllByBudgetId(budget.getId());
        this.onBack = onBack;

        initComponents();
        setListeners();
        refreshExpenses();
    }

    private void setListeners() {
        backButton.addActionListener(e -> {
            if (onBack != null) {
                onBack.run();
            }
            mainFrame.showScreen(ScreenNames.NAVIGATION_SCREEN);
        });
        participantsButton.addActionListener(e -> {
            ParticipantsDialog participantsDialog = new ParticipantsDialog(mainFrame, budget);
            participantsDialog.setVisible(true);
        });
        if (editButton != null) {
            editButton.addActionListener(e -> mainFrame.registerAndShowScreen(ScreenNames.BUDGET_FORM,
                    new BudgetForm(mainFrame, budget, mainFrame.getCurrentScreen(), this::refreshExpenses)));
        }
        if (shareButton != null) {
            shareButton.addActionListener(e -> mainFrame.registerAndShowScreen(
                    ScreenNames.BUDGET_SHARE_FORM, new ShareBudgetForm(mainFrame, budget)));
        }
        if (addExpenseButton != null) {
            addExpenseButton.addActionListener(e -> mainFrame.registerAndShowScreen(
                    ScreenNames.EXPENSE_FORM,
                    new ExpenseForm(mainFrame, null, budget,
                            mainFrame.getCurrentScreen(),
                            this::refreshExpenses
                    )
            ));
        }
        if (importButton != null) {
            importButton.addActionListener(e -> openImport(mainFrame, budget, this::refreshExpenses));
        }
        expensesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = expensesTable.getSelectedRow();
                if (selectedRow != -1) {
                    Expense selectedExpense = expenses.get(selectedRow);
                    mainFrame.registerAndShowScreen(ScreenNames.EXPENSE_DETAIL_VIEW,
                            new ExpenseDetailView(mainFrame, selectedExpense,
                                    mainFrame.getCurrentScreen(), this::refreshExpenses
                            )
                    );
                    expensesTable.clearSelection();
                }
            }
        });
        favoriteCheckBox.addActionListener(e -> {
            budgetAccess.setFavorite(favoriteCheckBox.isSelected());
            budgetAccessService.update(budgetAccess);
        });
        auditButton.addActionListener(e -> mainFrame.registerAndShowScreen(
                ScreenNames.BUDGET_AUDIT_DETAIL_VIEW,
                new BudgetAuditListView(mainFrame, budget)
        ));
    }

    public static void openImport(MainFrame mainFrame, Budget budget, Runnable onImportSuccess) {
        if (mainFrame.getScreen(ScreenNames.EXPENSES_IMPORT) == null) {
            ExpensesImportScreen importScreen = new ExpensesImportScreen(mainFrame, budget, mainFrame.getCurrentScreen(), onImportSuccess);
            mainFrame.registerAndShowScreen(ScreenNames.EXPENSES_IMPORT, importScreen);
        } else {
            int option = JOptionPane.showConfirmDialog(
                    mainFrame,
                    THERE_IS_ON_GOING_IMPORT,
                    IMPORT_EXPENSES,
                    JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.NO_OPTION) {
                ExpensesImportSharedData.resetInstance();
                mainFrame.removeScreen(ScreenNames.EXPENSES_IMPORT);
                mainFrame.registerAndShowScreen(
                        ScreenNames.EXPENSES_IMPORT,
                        new ExpensesImportScreen(mainFrame, budget, mainFrame.getCurrentScreen(), onImportSuccess)
                );
            } else {
                mainFrame.showScreen(ScreenNames.EXPENSES_IMPORT);
            }
        }
    }

    private void refreshExpenses() {
        int offset = currentPage * PAGE_SIZE;
        expenses.clear();
        expenses.addAll(mainFrame.getContext().getBean(ExpenseService.class)
                .getExpensesByBudgetIdWithPagination(budget.getId(), offset, PAGE_SIZE));

        expensesTable.setModel(createExpensesTableModel(expenses));
        updatePaginationPanel();
    }

    private void updatePaginationPanel() {
        paginationPanel.removeAll();
        long totalExpenses = mainFrame.getContext().getBean(ExpenseService.class).getCountByBudgetId(budget.getId());
        int totalPages = (int) Math.ceil((double) totalExpenses / PAGE_SIZE);
        if (totalPages > 1) {
            for (int i = 0; i < totalPages; i++) {
                int pageIndex = i;
                JButton pageButton = new JButton(String.valueOf(i + 1));
                pageButton.addActionListener(e -> {
                    currentPage = pageIndex;
                    refreshExpenses();
                });
                paginationPanel.add(pageButton);
            }
        }
        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private DefaultTableModel createExpensesTableModel(List<Expense> expenses) {
        String[] columnNames = {TITLE, AMOUNT, CATEGORY, DATE, CREATED_BY_COLUMN};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Expense expense : expenses) {
            model.addRow(new Object[]{
                    expense.getTitle(),
                    expense.getAmount(),
                    expense.getCategory(),
                    expense.getDate(),
                    expense.getCreatedBy().getName()});
        }
        return model;
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Painel de Detalhes do Orçamento
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder(BUDGET_DETAILS));

        nameLabel = new JLabel(NAME + ": " + budget.getName());
        descriptionLabel = new JLabel(DESCRIPTION + ": " + budget.getDescription());
        if (budget.getCreatedBy() != null) {
            createdByLabel = new JLabel(CREATED_BY + ": " + budget.getCreatedBy().getName());
        } else {
            createdByLabel = new JLabel(CREATED_BY + ": " + localeManager.getTranslation("system"));
        }

        Font font = new Font("Arial", Font.PLAIN, 14);
        nameLabel.setFont(font);
        descriptionLabel.setFont(font);
        createdByLabel.setFont(font);

        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(descriptionLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(createdByLabel);

        // Checkbox for favorites
        favoriteCheckBox = new JCheckBox(FAVORITE);
        favoriteCheckBox.setSelected(budgetAccess.isFavorite());
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(favoriteCheckBox);

        // Painel para botões próximos aos detalhes (alinhados à direita)
        JPanel topButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        participantsButton = new JButton(PARTICIPANTS);
        topButtonsPanel.add(participantsButton);

        boolean hasOwnerAccess = budgetAccessService.hasAccess(budget.getId(), user.getId(), BudgetAccessLevel.OWNER);
        boolean hasEditorAccess = budgetAccessService.hasAccess(budget.getId(), user.getId(), BudgetAccessLevel.EDITOR);

        if (hasEditorAccess) {
            editButton = new JButton(EDIT_BUDGET);
            topButtonsPanel.add(editButton);
            importButton = new JButton(IMPORT_EXPENSES);
            topButtonsPanel.add(importButton);
        }
        if (hasOwnerAccess) {
            shareButton = new JButton(SHARE_BUDGET);
            topButtonsPanel.add(shareButton);
        }

        // Add the audit button
        auditButton = new JButton(ACTIVITY);
        //topButtonsPanel.add(auditButton);

        // Adiciona os detalhes e os botões ao topo
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(detailsPanel, BorderLayout.CENTER);
        topPanel.add(topButtonsPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Painel Central (Tabela de Despesas)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder(EXPENSES));

        // Configurar o JScrollPane para a tabela
        expensesTable = new JTable(createExpensesTableModel(new ArrayList<>()));
        expensesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Certifica-se de ajustar as colunas corretamente
        JScrollPane scrollPane = new JScrollPane(expensesTable);

        // Configurar políticas de barras de rolagem
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Painel Inferior dividido para Back e Simulate Budget
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Painel do botão "Back" (alinhado à esquerda)
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = new JButton(BACK);
        leftButtonPanel.add(backButton);
        bottomPanel.add(leftButtonPanel, BorderLayout.WEST);

        // Painel do botão "Simulate Budget" e "Statistics" (alinhado à direita)
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if (!expenses.isEmpty()) {
            // Adicionar botão de estatísticas
            JButton statisticsButton = new JButton(STATISTICS);
            statisticsButton.addActionListener(e -> mainFrame.registerAndShowScreen(
                    ScreenNames.BUDGET_STATISTICS_VIEW, new BudgetStatisticsView(mainFrame, budget)));
            rightButtonPanel.add(statisticsButton);
        }

        if (!expenses.isEmpty()) {
            JButton splitSimulation = new JButton(SPLIT_SIMULATION);
            splitSimulation.addActionListener(e -> mainFrame.registerAndShowScreen(
                    ScreenNames.SIMULATE_VIEW, new SimulationView(mainFrame, budget)));
            rightButtonPanel.add(splitSimulation);
        }
        if (hasEditorAccess) {
            addExpenseButton = new JButton(ADD_EXPENSE);
            rightButtonPanel.add(addExpenseButton);
        }

        bottomPanel.add(rightButtonPanel, BorderLayout.EAST);

        // Adicionar painel de paginação
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(paginationPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private JPanel paginationPanel;
    private JTable expensesTable;
    private JCheckBox favoriteCheckBox;
    JLabel nameLabel, descriptionLabel, createdByLabel;
    private JButton auditButton, backButton, participantsButton, editButton, shareButton, addExpenseButton, importButton;
    private static final LocaleManager localeManager = LocaleManager.getInstance();
    private static final String
            BUDGET_DETAILS = localeManager.getTranslation("budget_details"),
            NAME = localeManager.getTranslation("name"),
            DESCRIPTION = localeManager.getTranslation("description"),
            CREATED_BY = localeManager.getTranslation("created_by"),
            FAVORITE = localeManager.getTranslation("favorite"),
            PARTICIPANTS = localeManager.getTranslation("participants"),
            EDIT_BUDGET = localeManager.getTranslation("edit_budget"),
            SHARE_BUDGET = localeManager.getTranslation("share_budget"),
            IMPORT_EXPENSES = localeManager.getTranslation("import_expenses"),
            ACTIVITY = localeManager.getTranslation("activity"),
            EXPENSES = localeManager.getTranslation("expenses"),
            TITLE = localeManager.getTranslation("title"),
            AMOUNT = localeManager.getTranslation("amount"),
            CATEGORY = localeManager.getTranslation("category"),
            DATE = localeManager.getTranslation("date"),
            CREATED_BY_COLUMN = localeManager.getTranslation("created_by_column"),
            BACK = localeManager.getTranslation("back"),
            STATISTICS = localeManager.getTranslation("statistics"),
            SPLIT_SIMULATION = localeManager.getTranslation("split_simulation"),
            ADD_EXPENSE = localeManager.getTranslation("add_expense"),
            THERE_IS_ON_GOING_IMPORT = localeManager.getTranslation("there_is_ongoing_import");
}