package com.ptda.tracker.ui.renderers;

import com.ptda.tracker.models.tracker.Expense;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public class ExpenseListRenderer extends JPanel implements ListCellRenderer<Expense> {

    private JLabel amountLabel;
    private JLabel titleLabel;
    private JLabel categoryLabel;
    private JLabel dateLabel;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    private static final String
            NO_TITLE = "No Title",
            OTHER = "OTHER";

    public ExpenseListRenderer() {
        setLayout(new BorderLayout(10, 10));

        // Labels for rendering
        amountLabel = new JLabel();
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        categoryLabel = new JLabel();
        categoryLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        dateLabel = new JLabel();
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Left panel for main content
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(amountLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(titleLabel);

        // Right panel for category and date
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(categoryLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(dateLabel);

        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Expense> list,
            Expense expense,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        // Format and set values
        amountLabel.setText(String.format("€%.2f", expense.getAmount()));
        titleLabel.setText(expense.getTitle() != null ? expense.getTitle() : NO_TITLE);
        categoryLabel.setText(expense.getCategory() != null ? expense.getCategory().toString() : OTHER);

        // Format date
        dateLabel.setText(expense.getDate() != null ? DATE_FORMAT.format(expense.getDate()) : DATE_FORMAT.format(expense.getCreatedAt()));

        return this;
    }
}