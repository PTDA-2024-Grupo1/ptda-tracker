package com.ptda.tracker.ui.user.components.tables;

import com.ptda.tracker.models.tracker.Budget;
import com.ptda.tracker.models.tracker.Expense;
import com.ptda.tracker.models.tracker.ExpenseCategory;
import com.ptda.tracker.util.LocaleManager;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.ptda.tracker.ui.user.forms.ExpensesEditForm.createBudgetMap;

public class ExpensesTableModel extends AbstractTableModel {
    @Getter
    private final List<Expense> expenses;
    private final Map<String, Budget> budgetMap;

    public ExpensesTableModel(List<Expense> expenses, List<Budget> budgets) {
        this.expenses = expenses;
        this.budgetMap = createBudgetMap(budgets);
    }

    @Override
    public int getRowCount() {
        return expenses.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Expense expense = expenses.get(rowIndex);
        switch (columnIndex) {
            case 0: return expense.getTitle();
            case 1: return expense.getAmount();
            case 2: return expense.getDate();
            case 3: return expense.getCategory() != null ? expense.getCategory() : ExpenseCategory.OTHER; // Default to OTHER
            case 4: return expense.getDescription();
            case 5: return expense.getBudget() != null ? expense.getBudget().getName() : ""; // Display only the budget name
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Expense expense = expenses.get(rowIndex);
        switch (columnIndex) {
            case 0: expense.setTitle((String) aValue); break;
            case 1: expense.setAmount(((Number) aValue).doubleValue()); break;
            case 2: expense.setDate((Date) aValue); break;
            case 3: expense.setCategory((ExpenseCategory) aValue); break;
            case 4: expense.setDescription((String) aValue); break;
            case 5:
                if (aValue instanceof Budget) {
                    expense.setBudget((Budget) aValue);
                }
                break;
            default:
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true; // All cells are editable
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 1 -> Double.class; // Amount
            case 2 -> Date.class; // Date
            case 3 -> ExpenseCategory.class; // Category dropdown
            case 5 -> String.class; // Budget name as String
            default -> String.class;
        };
    }

    private Budget findBudgetByName(String name) {
        return budgetMap.get(name);
    }

    private static final LocaleManager localeManager = LocaleManager.getInstance();
    private static final String[] columnNames = {
            localeManager.getTranslation("title"),
            localeManager.getTranslation("amount"),
            localeManager.getTranslation("date"),
            localeManager.getTranslation("category"),
            localeManager.getTranslation("description"),
            localeManager.getTranslation("budget")
    };
}
