package com.ptda.tracker.ui;

import com.ptda.tracker.TrackerApplication;
import com.ptda.tracker.config.AppConfig;
import com.ptda.tracker.theme.ThemeManager;
import com.ptda.tracker.ui.dialogs.AboutDialog;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JCheckBoxMenuItem lightTheme, darkTheme;
    private final ThemeManager themeManager;

    private final Map<String, JPanel> screens;
    @Getter
    private final ApplicationContext context;
    @Getter
    private String currentScreen;

    private static final String
            NAVIGATION_SCREEN = "navigationScreen",
            SCREEN_NOT_FOUND = "Screen not found: ",
            ERROR = "Error",
            TITLE = "Divi - Turn your expenses into Achievements.";

    public MainFrame(ApplicationContext context) {
        this.context = context; // Inject Spring context
        this.screens = new HashMap<>();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);

        setJMenuBar(createMenuBar());
        themeManager = new ThemeManager(this);
        themeManager.setTheme(getThemePreference());
        if (themeManager.isDark()) {
            darkTheme.setSelected(true);
        } else {
            lightTheme.setSelected(true);
        }

        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        add(mainPanel);
    }

    public void registerScreen(String name, JPanel screen) {
        screens.put(name, screen);
        mainPanel.add(screen, name);
    }

    public void showScreen(String name) {
        if (screens.containsKey(name)) {
            cardLayout.show(mainPanel, name);
            this.currentScreen = name;
        } else {
            JOptionPane.showMessageDialog(this, SCREEN_NOT_FOUND + name, ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    public void registerAndShowScreen(String name, JPanel screen) {
        registerScreen(name, screen);
        showScreen(name);
    }

    public void removeScreen(String screenName) {
        screens.remove(screenName);
    }

    public void setCurrentScreen(String screen) {
        // Notify NavigationMenu to update highlights
        if (screens.get("navMenu") != null) {
            ((NavigationMenu) screens.get("navMenu")).updateActiveScreen(screen);
        }
    }

    public JPanel getScreen(String screenName) {
        return screens.get(screenName);
    }

    private JMenuBar createMenuBar() {
        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create "File" menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        // Create "Theme" menu
        JMenu themeMenu = new JMenu("Theme");

        lightTheme = new JCheckBoxMenuItem();
        lightTheme.addActionListener(this::lightThemeClicked);
        lightTheme.setText("Light");
        themeMenu.add(lightTheme);

        darkTheme = new JCheckBoxMenuItem();
        darkTheme.addActionListener(this::darkThemeClicked);
        darkTheme.setText("Dark");
        themeMenu.add(darkTheme);

        // Create "Help" menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(e -> new AboutDialog(this).setVisible(true));
        helpMenu.add(aboutMenuItem);

        // Add menus to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void lightThemeClicked(ActionEvent e) {
        lightTheme.setSelected(true);
        darkTheme.setSelected(false);
        themeManager.setTheme(AppConfig.DEFAULT_LIGHT_THEME);
        setThemePreference(AppConfig.DEFAULT_LIGHT_THEME);
    }

    private void darkThemeClicked(ActionEvent e) {
        lightTheme.setSelected(false);
        darkTheme.setSelected(true);
        themeManager.setTheme(AppConfig.DEFAULT_DARK_THEME);
        setThemePreference(AppConfig.DEFAULT_DARK_THEME);
    }

    private void setThemePreference(String theme) {
        Preferences preferences = Preferences.userNodeForPackage(TrackerApplication.class);
        preferences.put("theme", theme);
    }

    private String getThemePreference() {
        Preferences preferences = Preferences.userNodeForPackage(TrackerApplication.class);
        return preferences.get("theme", AppConfig.DEFAULT_LIGHT_THEME);
    }
}