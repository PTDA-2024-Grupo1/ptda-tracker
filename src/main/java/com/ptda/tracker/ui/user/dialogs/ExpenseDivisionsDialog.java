package com.ptda.tracker.ui.user.dialogs;

import com.ptda.tracker.models.tracker.ExpenseDivision;

import javax.swing.*;
import java.util.List;

public class ExpenseDivisionsDialog extends JDialog {
    private final List<ExpenseDivision> expenseDivisions;

    public ExpenseDivisionsDialog(List<ExpenseDivision> expenseDivisions) {
        this.expenseDivisions = expenseDivisions;
        initUI();
    }

    private void initUI() {
        setTitle(TITLE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        divisionsTable = createDivisionsTable(expenseDivisions);
        JScrollPane scrollPane = new JScrollPane(divisionsTable);
        add(scrollPane);
    }

    private JTable createDivisionsTable(List<ExpenseDivision> expenseDivisions) {
        return createDivisionsJTable(expenseDivisions);
    }

    public static JTable createDivisionsJTable(List<ExpenseDivision> expenseDivisions) {
        String[] columnNames = {ID, AMOUNT, PERCENTAGE, CREATED_BY, ASSOCIATED_USER};
        Object[][] data = new Object[expenseDivisions.size()][columnNames.length];
        for (int i = 0; i < expenseDivisions.size(); i++) {
            ExpenseDivision expenseDivision = expenseDivisions.get(i);
            data[i][0] = expenseDivision.getId();
            data[i][1] = expenseDivision.getAmount() + "€";
            data[i][3] = expenseDivision.getCreatedBy().getName();
            data[i][4] = expenseDivision.getUser().getName();
        }
        return new JTable(data, columnNames);
    }

    private JTable divisionsTable;
    private static final String
            TITLE = "Subdivisions",
            ID = "ID",
            AMOUNT = "Amount",
            PERCENTAGE = "Percentage",
            CREATED_BY = "Created By",
            ASSOCIATED_USER = "Associated User";
}