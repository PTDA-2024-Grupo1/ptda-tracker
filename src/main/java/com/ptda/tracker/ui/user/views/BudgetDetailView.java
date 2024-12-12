package com.ptda.tracker.ui.user.views;

import com.ptda.tracker.models.tracker.Budget;
import com.ptda.tracker.models.tracker.BudgetAccessLevel;
import com.ptda.tracker.models.tracker.Expense;
import com.ptda.tracker.models.tracker.ExpenseCategory;
import com.ptda.tracker.models.user.User;
import com.ptda.tracker.services.tracker.BudgetAccessService;
import com.ptda.tracker.services.tracker.BudgetService;
import com.ptda.tracker.services.tracker.ExpenseService;
import com.ptda.tracker.ui.MainFrame;
import com.ptda.tracker.ui.user.dialogs.ParticipantsDialog;
import com.ptda.tracker.ui.user.forms.BudgetForm;
import com.ptda.tracker.ui.user.forms.ExpenseForm;
import com.ptda.tracker.ui.user.forms.ShareBudgetForm;
import com.ptda.tracker.util.ScreenNames;
import com.ptda.tracker.util.UserSession;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BudgetDetailView extends JPanel {
    private final MainFrame mainFrame;
    private final BudgetAccessService budgetAccessService;
    private final User user = UserSession.getInstance().getUser();
    private final Budget budget;
    private final List<Expense> expenses;

    public BudgetDetailView(MainFrame mainFrame, Budget budget) {
        this.mainFrame = mainFrame;
        budgetAccessService = mainFrame.getContext().getBean(BudgetAccessService.class);
        this.budget = budget;
        expenses = mainFrame.getContext().getBean(ExpenseService.class).getAllByBudgetId(budget.getId());

        initComponents();
        setListeners();
        refreshExpenses();
    }

    private void setListeners() {
        backButton.addActionListener(e -> mainFrame.showScreen(ScreenNames.NAVIGATION_SCREEN));
        participantsButton.addActionListener(e -> {
            ParticipantsDialog participantsDialog = new ParticipantsDialog(mainFrame, budget);
            participantsDialog.setVisible(true);
        });
        if (editButton != null) {
            editButton.addActionListener(e -> mainFrame.registerAndShowScreen(ScreenNames.BUDGET_FORM, new BudgetForm(mainFrame, null, budget)));
        }
        if (shareButton != null) {
            shareButton.addActionListener(e -> mainFrame.registerAndShowScreen(ScreenNames.BUDGET_SHARE_FORM, new ShareBudgetForm(mainFrame, budget)));
        }
        if (addExpenseButton != null) {
            addExpenseButton.addActionListener(e -> mainFrame.registerAndShowScreen(ScreenNames.EXPENSE_FORM, new ExpenseForm(mainFrame, null, budget, mainFrame.getCurrentScreen(), this::refreshExpenses)));
        }
        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                importExpensesFromCSV(selectedFile);
            }
        });
        expensesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = expensesTable.getSelectedRow();
                if (selectedRow != -1) {
                    Expense selectedExpense = expenses.get(selectedRow);
                    mainFrame.registerAndShowScreen(ScreenNames.EXPENSE_DETAIL_VIEW, new ExpenseDetailView(mainFrame, selectedExpense, mainFrame.getCurrentScreen(), null));
                    expensesTable.clearSelection();
                }
            }
        });
    }

    private void importExpensesFromCSV(File file) {
        List<Expense> importedExpenses = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (FileReader reader = new FileReader(file);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            for (CSVRecord csvRecord : csvParser) {
                Expense expense = new Expense();
                expense.setTitle(csvRecord.get("title"));
                expense.setAmount(Double.parseDouble(csvRecord.get("amount")));
                LocalDate localDate = LocalDate.parse(csvRecord.get("date"), formatter);
                expense.setDate(java.sql.Date.valueOf(localDate));
                expense.setCategory(convertToExpenseCategory(csvRecord.get("category")));
                expense.setDescription(csvRecord.get("description"));
                expense.setCreatedBy(UserSession.getInstance().getUser());
                expense.setBudget(budget);
                importedExpenses.add(expense);
            }

            mainFrame.getContext().getBean(ExpenseService.class).saveAll(importedExpenses);
            JOptionPane.showMessageDialog(this, "Expenses imported successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading CSV file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error importing expenses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // After importing, refresh the expense list
        refreshExpenses();
    }

    private ExpenseCategory convertToExpenseCategory(String category) {
        try {
            return ExpenseCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle the case where the category does not match any enum constant
            JOptionPane.showMessageDialog(this, "Unknown category: " + category, "Error", JOptionPane.ERROR_MESSAGE);
            return null; // or handle it in another way
        }
    }

    private void refreshExpenses() {
        int offset = currentPage * PAGE_SIZE;
        expenses.clear();
        expenses.addAll(mainFrame.getContext().getBean(ExpenseService.class).getExpensesByBudgetIdWithPagination(budget.getId(), offset, PAGE_SIZE));
        updateExpenseTable();
    }

    private void updateExpenseTable() {
        expensesTable.setModel(createExpensesTableModel(expenses));
        updatePaginationPanel();
    }

    private void updatePaginationPanel() {
        paginationPanel.removeAll();
        long totalExpenses = mainFrame.getContext().getBean(ExpenseService.class).countByBudgetId(budget.getId());
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
        createdByLabel = new JLabel(CREATED_BY + ": " + budget.getCreatedBy().getName());

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
        JCheckBox favoriteCheckBox = new JCheckBox("Favorite");
        favoriteCheckBox.setSelected(budget.isFavorite());
        favoriteCheckBox.addActionListener(e -> {
            budget.setFavorite(favoriteCheckBox.isSelected());
            mainFrame.getContext().getBean(BudgetService.class).update(budget);
        });
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
        }
        if (hasOwnerAccess) {
            shareButton = new JButton(SHARE_BUDGET);
            topButtonsPanel.add(shareButton);
        }

        importButton = new JButton("Import Expenses");
        topButtonsPanel.add(importButton);

        // Add the audit button
        auditButton = new JButton("Audit Changes");
        auditButton.addActionListener(e -> mainFrame.registerAndShowScreen(ScreenNames.BUDGET_AUDIT_DETAIL_VIEW, new BudgetAuditDetailView(mainFrame, budget)));
        topButtonsPanel.add(auditButton);

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

        // Painel do botão "Simulate Budget" (alinhado à direita)
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (!expenses.isEmpty()) {
            JButton splitSimulation = new JButton(SPLIT_SIMULATION);
            splitSimulation.addActionListener(e -> mainFrame.registerAndShowScreen(ScreenNames.SIMULATE_VIEW, new SimulationView(mainFrame, budget)));
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
    JLabel nameLabel, descriptionLabel, createdByLabel;
    private JButton auditButton, backButton, participantsButton, editButton, shareButton, addExpenseButton, importButton;
    private static final String
            BUDGET_DETAILS = "Budget Details",
            NAME = "Name",
            DESCRIPTION = "Description",
            CREATED_BY = "Created By",
            EXPENSES = "Expenses",
            BACK = "Back",
            PARTICIPANTS = "Participants",
            EDIT_BUDGET = "Edit Budget",
            ADD_EXPENSE = "Add Expense",
            SHARE_BUDGET = "Share Budget",
            SPLIT_SIMULATION = "Split Simulation",
            TITLE = "Title",
            AMOUNT = "Amount",
            CATEGORY = "Category",
            DATE = "Date",
            CREATED_BY_COLUMN = "Created By";
}