package com.ptda.tracker.ui;

import com.ptda.tracker.config.AppConfig;
import com.ptda.tracker.theme.ThemeManager;
import com.ptda.tracker.ui.user.dialogs.AboutDialog;
import com.ptda.tracker.ui.user.dialogs.ChooseLanguageDialog;
import com.ptda.tracker.util.ImageResourceManager;
import com.ptda.tracker.util.LocaleManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;


public class MainFrame extends JFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainFrame.class);

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JCheckBoxMenuItem lightTheme, darkTheme;
    private final ThemeManager themeManager;

    private final Map<String, JPanel> screens;
    @Getter
    private final ApplicationContext context;
    @Getter
    private String currentScreen;

    private static final LocaleManager localeManager = LocaleManager.getInstance();
    private static final String
            SCREEN_NOT_FOUND = localeManager.getTranslation("screenNotFound"),
            ERROR = localeManager.getTranslation("error"),
            TITLE = localeManager.getTranslation("divi_expense_tracker"),
            LANGUAGE = localeManager.getTranslation("language"),
            FILE = localeManager.getTranslation("file"),
            EXIT = localeManager.getTranslation("exit"),
            THEME = localeManager.getTranslation("theme"),
            LIGHT = localeManager.getTranslation("light"),
            DARK = localeManager.getTranslation("dark"),
            HELP = localeManager.getTranslation("help"),
            ABOUT = localeManager.getTranslation("about");


    public MainFrame(ApplicationContext context) {
        LOGGER.debug("Initializing MainFrame...");
        this.context = context;
        this.screens = new HashMap<>();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);

        LOGGER.debug("Initializing ThemeManager...");
        themeManager = new ThemeManager(this);
        themeManager.setTheme(getThemePreference());

        LOGGER.debug("Setting up menu bar...");
        setJMenuBar(createMenuBar());

        if (themeManager.isDark()) {
            darkTheme.setSelected(true);
        } else {
            lightTheme.setSelected(true);
        }

        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        // Add components to the frame
        add(mainPanel, BorderLayout.CENTER);

        LOGGER.debug("MainFrame initialized successfully.");
    }


    public void registerAndShowScreen(String name, JPanel screen) {
        // Remove existing screen if present to allow reinitialization
        if (screens.containsKey(name)) {
            removeScreen(name);
        }
        registerScreen(name, screen);
        showScreen(name);
    }


    public void registerScreen(String name, JPanel screen) {
        screens.put(name, screen);
        mainPanel.add(screen, name);
        LOGGER.debug("Registered screen: {}", name);
    }


    public JPanel getScreen(String screenName) {
        return screens.get(screenName);
    }

    public void showScreen(String name) {
        LOGGER.debug("Attempting to show screen: {}", name);
        if (screens.containsKey(name)) {
            cardLayout.show(mainPanel, name);
            this.currentScreen = name;
            LOGGER.debug("Screen {} displayed successfully.", name);
        } else {
            JOptionPane.showMessageDialog(this, SCREEN_NOT_FOUND + ": " + name, ERROR, JOptionPane.ERROR_MESSAGE);
            LOGGER.error("Screen not found: {}", name);
        }
    }


    public void removeScreen(String screenName) {
        LOGGER.debug("Removing screen: {}", screenName);
        JPanel screen = screens.remove(screenName);
        if (screen != null) {
            mainPanel.remove(screen);
            mainPanel.revalidate();
            mainPanel.repaint();
            LOGGER.debug("Screen {} removed successfully.", screenName);
        } else {
            LOGGER.warn("Attempted to remove non-existing screen: {}", screenName);
        }
    }


    private JMenuBar createMenuBar() {
        LOGGER.debug("Creating menu bar...");
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu(FILE);
        JMenuItem languageMenuItem = new JMenuItem(LANGUAGE);
        languageMenuItem.addActionListener(e -> new ChooseLanguageDialog(this).setVisible(true));
        fileMenu.add(languageMenuItem);
        JMenuItem exitMenuItem = new JMenuItem(EXIT);
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        // Theme Menu
        JMenu themeMenu = new JMenu(THEME);
        lightTheme = new JCheckBoxMenuItem(LIGHT);
        lightTheme.addActionListener(this::lightThemeClicked);
        themeMenu.add(lightTheme);

        darkTheme = new JCheckBoxMenuItem(DARK);
        darkTheme.addActionListener(this::darkThemeClicked);
        themeMenu.add(darkTheme);

        // Help Menu
        JMenu helpMenu = new JMenu(HELP);
        JMenuItem aboutMenuItem = new JMenuItem(ABOUT);
        aboutMenuItem.addActionListener(e -> new AboutDialog(this).setVisible(true));
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        menuBar.add(helpMenu);

        LOGGER.debug("Menu bar created successfully.");
        return menuBar;
    }


    private void lightThemeClicked(ActionEvent e) {
        LOGGER.debug("Light theme selected.");
        lightTheme.setSelected(true);
        darkTheme.setSelected(false);
        themeManager.setTheme(AppConfig.DEFAULT_LIGHT_THEME);
        setThemePreference(AppConfig.DEFAULT_LIGHT_THEME);
    }


    private void darkThemeClicked(ActionEvent e) {
        LOGGER.debug("Dark theme selected.");
        lightTheme.setSelected(false);
        darkTheme.setSelected(true);
        themeManager.setTheme(AppConfig.DEFAULT_DARK_THEME);
        setThemePreference(AppConfig.DEFAULT_DARK_THEME);
    }


    private void setThemePreference(String theme) {
        LOGGER.debug("Saving theme preference: {}", theme);
        Preferences preferences = Preferences.userNodeForPackage(MainFrame.class);
        preferences.put("theme", theme);
    }
    private String getThemePreference() {
        Preferences preferences = Preferences.userNodeForPackage(MainFrame.class);
        String theme = preferences.get("theme", AppConfig.DEFAULT_LIGHT_THEME);
        LOGGER.debug("Retrieved theme preference: {}", theme);
        return theme;
    }
}