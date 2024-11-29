package com.ptda.tracker;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import com.ptda.tracker.config.AppConfig;
import com.ptda.tracker.models.user.User;
import com.ptda.tracker.services.user.UserService;
import com.ptda.tracker.ui.MainFrame;
import com.ptda.tracker.ui.forms.LoginForm;
import com.ptda.tracker.ui.screens.CustomSplashScreen;
import com.ptda.tracker.util.ScreenNames;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.Preferences;

@SpringBootApplication
public class TrackerApplication {

	public static void main(String[] args) {
        try {
            setLookAndFeel();
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        ApplicationContext context = SpringApplication.run(TrackerApplication.class, args);
		context.getBean(DataInit.class).init();

		CustomSplashScreen splashScreen = new CustomSplashScreen();
		splashScreen.showSplashScreen();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			MainFrame mainFrame = new MainFrame(context);

			Preferences preferences = Preferences.userNodeForPackage(TrackerApplication.class);
			String username = preferences.get("email", null);
			String encryptedPassword = preferences.get("password", null);

			UserService userService = context.getBean(UserService.class);
			Optional<User> user = userService.getByEmail(username);

			if (user.isPresent() && Objects.equals(encryptedPassword, user.get().getPassword())) {
				LoginForm.onAuthSuccess(user.get(), mainFrame);
			} else {
				mainFrame.registerAndShowScreen(ScreenNames.LOGIN_FORM, new LoginForm(mainFrame));
			}

			splashScreen.hideSplashScreen();
			mainFrame.setVisible(true);
		});
	}

	private static void setLookAndFeel() throws UnsupportedLookAndFeelException {
		if (SystemInfo.isMacOS) {
			// enable screen menu bar
			// (moves menu bar from JFrame window to top of screen)
			System.setProperty("apple.laf.useScreenMenuBar", "true");

			// application name used in screen menu bar
			// (in first menu after the "apple" menu)
			System.setProperty("apple.awt.application.name", AppConfig.APP_NAME);

			// appearance of window title bars
			// possible values:
			//   - "system": use current macOS appearance (light or dark)
			//   - "NSAppearanceNameAqua": use light appearance
			//   - "NSAppearanceNameDarkAqua": use dark appearance
			// (needs to be set on main thread; setting it on AWT thread does not work)
			System.setProperty("apple.awt.application.appearance", "system");
		} else if (SystemInfo.isLinux) {
			// enable custom window decorations
			JFrame.setDefaultLookAndFeelDecorated(true);
			JDialog.setDefaultLookAndFeelDecorated(true);

			try {
				var toolkit = Toolkit.getDefaultToolkit();
				var awtAppClassNameField = toolkit.getClass().getDeclaredField("awtAppClassName");
				awtAppClassNameField.setAccessible(true);
				awtAppClassNameField.set(toolkit, AppConfig.APP_NAME);
			} catch (NoSuchFieldException | InaccessibleObjectException | IllegalAccessException e) {
				//LOGGER.debug("Failed to set proper app name");
			}
		}

		UIManager.setLookAndFeel(new FlatLightLaf());

		FlatLaf.registerCustomDefaultsSource("com.ptda.tracker.theme.custom");

		final int rounding = 8;
		final int insets = 2;

		UIManager.put("CheckBox.icon.style", "filled");
		UIManager.put("Component.arrowType", "chevron");

		UIManager.put("Component.focusWidth", 1);
		UIManager.put("Component.innerFocusWidth", 1);

		UIManager.put("Button.arc", rounding);
		UIManager.put("Component.arc", rounding);
		UIManager.put("ProgressBar.arc", rounding);
		UIManager.put("TextComponent.arc", rounding);

		UIManager.put("ScrollBar.thumbArc", rounding);
		UIManager.put("ScrollBar.thumbInsets", new Insets(insets, insets, insets, insets));

		// Set default font
		Font defaultFont = new Font("Arial", Font.PLAIN, 14);
		UIManager.put("defaultFont", defaultFont);

		System.setProperty("java.awt.headless", "false");
	}
}
