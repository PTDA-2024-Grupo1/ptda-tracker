package com.ptda.tracker.ui.forms;

import com.ptda.tracker.models.user.EmailVerification;
import com.ptda.tracker.models.user.User;
import com.ptda.tracker.services.email.EmailService;
import com.ptda.tracker.services.user.EmailVerificationService;
import com.ptda.tracker.services.user.UserService;
import com.ptda.tracker.ui.MainFrame;
import com.ptda.tracker.util.LocaleManager;

import javax.swing.*;
import java.awt.*;

public class EmailVerificationForm extends JPanel {
    private final MainFrame mainFrame;
    private final EmailVerificationService emailVerificationService;
    private final User newUser;
    private final String returnScreen;

    public EmailVerificationForm(MainFrame mainFrame, User newUser, String returnScreen) {
        new Thread(this::sendVerificationCode).start();
        this.mainFrame = mainFrame;
        this.emailVerificationService = mainFrame.getContext().getBean(EmailVerificationService.class);
        this.newUser = newUser;
        this.returnScreen = returnScreen;
        initComponents();
        setListeners();
    }

    private void setListeners() {
        cancelButton.addActionListener(e -> {
            mainFrame.showScreen(returnScreen);
        });
        verifyButton.addActionListener(e -> {
            if (verifyEmail()) {
                LoginForm.onAuthSuccess(newUser, mainFrame);
                JOptionPane.showMessageDialog(
                        this,
                        REGISTRATION_SUCCESSFUL,
                        SUCCESS,
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    }

    private void sendVerificationCode() {
        EmailVerification emailVerification = emailVerificationService.create(newUser.getEmail());
        EmailService emailService = mainFrame.getContext().getBean(EmailService.class);
        String subject = EMAIL_VERIFICATION;
        String message = VERIFICATION_CODE_MESSAGE + ": " + emailVerification.getCode();
        emailService.sendEmail(newUser.getEmail(), subject, message);
    }

    private boolean verifyEmail() {
        String verificationCode = verificationCodeField.getText();
        EmailVerification emailVerification = emailVerificationService.
                getByEmail(newUser.getEmail()).orElse(null);
        if (emailVerification == null) {
            JOptionPane.showMessageDialog(
                    this,
                    VERIFICATION_CODE_INCORRECT,
                    ERROR,
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        if (emailVerification.isUsed()) {
            JOptionPane.showMessageDialog(
                    this,
                    VERIFICATION_CODE_ALREADY_USED,
                    ERROR,
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        if (!emailVerification.getCode().equals(verificationCode)) {
            JOptionPane.showMessageDialog(
                    this,
                    INVALID_VERIFICATION_CODE,
                    ERROR,
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        UserService userService = mainFrame.getContext().getBean(UserService.class);
        newUser.setEmailVerified(true);
        userService.update(newUser);
        emailVerificationService.activate(emailVerification);
        JOptionPane.showMessageDialog(
                this,
                EMAIL_VERIFIED_SUCCESSFULLY,
                SUCCESS,
                JOptionPane.INFORMATION_MESSAGE
        );
        return true;
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        JLabel instructionLabel = new JLabel(ENTER_VERIFICATION_CODE_SENT + ": " + newUser.getEmail());
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        topPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between title and top
        topPanel.add(instructionLabel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between title and form

        add(topPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20)); // Internal margins

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Verification code field with label
        JLabel verificationCodeLabel = new JLabel(VERIFICATION_CODE + ":");
        verificationCodeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(verificationCodeLabel, gbc);

        verificationCodeField = new JTextField();
        verificationCodeField.setFont(new Font("Arial", Font.PLAIN, 14));
        verificationCodeField.setPreferredSize(new Dimension(200, 30));
        verificationCodeField.setToolTipText(ENTER_VERIFICATION_CODE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(verificationCodeField, gbc);

        // Verify button
        verifyButton = new JButton(VERIFY);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(verifyButton, gbc);

        // Back button
        cancelButton = new JButton(CANCEL);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(cancelButton, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private JTextField verificationCodeField;
    private JButton verifyButton, cancelButton;
    private static final LocaleManager localeManager = LocaleManager.getInstance();
    private static final String
            ENTER_VERIFICATION_CODE_SENT = localeManager.getTranslation("enter_verification_code_sent"),
            VERIFY = localeManager.getTranslation("verify"),
            CANCEL = localeManager.getTranslation("cancel"),
            REGISTRATION_SUCCESSFUL = localeManager.getTranslation("registration_successful"),
            SUCCESS = localeManager.getTranslation("success"),
            EMAIL_VERIFICATION = localeManager.getTranslation("email_verification"),
            ENTER_VERIFICATION_CODE = localeManager.getTranslation("enter_verification_code"),
            VERIFICATION_CODE = localeManager.getTranslation("verification_code"),
            VERIFICATION_CODE_MESSAGE = localeManager.getTranslation("verification_code_message"),
            VERIFICATION_CODE_INCORRECT = localeManager.getTranslation("verification_code_incorrect"),
            ERROR = localeManager.getTranslation("error"),
            VERIFICATION_CODE_ALREADY_USED = localeManager.getTranslation("verification_code_already_used"),
            INVALID_VERIFICATION_CODE = localeManager.getTranslation("invalid_verification_code"),
            EMAIL_VERIFIED_SUCCESSFULLY = localeManager.getTranslation("email_verified_successfully");
}