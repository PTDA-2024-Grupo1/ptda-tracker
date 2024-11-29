package com.ptda.tracker.ui.forms;

import com.ptda.tracker.models.assistance.Ticket;
import com.ptda.tracker.models.assistance.TicketReply;
import com.ptda.tracker.services.tracker.TicketReplyService;
import com.ptda.tracker.ui.MainFrame;
import com.ptda.tracker.ui.views.TicketDetailView;
import com.ptda.tracker.util.ScreenNames;
import com.ptda.tracker.util.UserSession;

import javax.swing.*;
import java.awt.*;

public class TicketReplyForm extends JPanel {
    private final MainFrame mainFrame;
    private final Ticket ticket;
    private final TicketReplyService ticketReplyService;
    private final Runnable onFormSubmit;

    private JTextArea replyBodyArea;

    private static final String
            REPLY_TO_TICKET = "Reply to Ticket: ",
            SAVE_REPLY = "Save Reply",
            CANCEL = "Cancel",
            REPLY_BODY_CANNOT_BE_EMPTY = "Reply body cannot be empty.",
            ERROR = "Error",
            REPLY_SAVED_SUCCESSFULLY = "Reply saved successfully.";

    public TicketReplyForm(MainFrame mainFrame, Runnable onFormSubmit, Ticket ticket) {
        this.mainFrame = mainFrame;
        this.ticket = ticket;
        this.onFormSubmit = (onFormSubmit != null) ? onFormSubmit : () -> {};
        this.ticketReplyService = mainFrame.getContext().getBean(TicketReplyService.class);

        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(REPLY_TO_TICKET + ticket.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        replyBodyArea = new JTextArea();
        replyBodyArea.setLineWrap(true);
        replyBodyArea.setWrapStyleWord(true);
        add(new JScrollPane(replyBodyArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton saveButton = new JButton(SAVE_REPLY);
        saveButton.addActionListener(e -> saveReply());

        JButton cancelButton = new JButton(CANCEL);
        cancelButton.addActionListener(e -> mainFrame.showScreen(ScreenNames.TICKET_DETAIL_VIEW));

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveReply() {
        String replyBody = replyBodyArea.getText().trim();
        if (replyBody.isEmpty()) {
            JOptionPane.showMessageDialog(this, REPLY_BODY_CANNOT_BE_EMPTY, ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }

        TicketReply reply = TicketReply.builder()
                .ticket(ticket)
                .createdBy(UserSession.getInstance().getUser())
                .body(replyBody)
                .build();
        ticketReplyService.save(reply);
        JOptionPane.showMessageDialog(this, REPLY_SAVED_SUCCESSFULLY);
        onFormSubmit.run();
        mainFrame.registerAndShowScreen(ScreenNames.TICKET_DETAIL_VIEW, new TicketDetailView(mainFrame, ticket));
    }
}