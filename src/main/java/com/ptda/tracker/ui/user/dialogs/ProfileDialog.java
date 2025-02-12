package com.ptda.tracker.ui.user.dialogs;

import com.ptda.tracker.models.user.User;
import com.ptda.tracker.util.LocaleManager;

import javax.swing.*;

public class ProfileDialog extends JDialog {
    private final User user;

    public ProfileDialog(User user) {
        this.user = user;
        initUI();
    }

    private void initUI() {
        setTitle(TITLE_PROFILE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(NAME + ": " + user.getName());
        JLabel emailLabel = new JLabel(EMAIL + ": " + user.getEmail());

        panel.add(nameLabel);
        panel.add(emailLabel);

        add(panel);
    }

    private static final LocaleManager localeManager = LocaleManager.getInstance();
    private static final String
            TITLE_PROFILE = localeManager.getTranslation("title.profile"),
            NAME = localeManager.getTranslation("name"),
            EMAIL = localeManager.getTranslation("email");
}