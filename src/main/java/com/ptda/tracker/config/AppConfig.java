package com.ptda.tracker.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.ptda.tracker")
public class AppConfig {
    public final static String APP_NAME = "Divi";
    public final static String LOGO_PATH = "src/main/resources/images/divi.png";
    public final static String FAVORITE_ICON_PATH = "/images/favorite.png";
    public final static String BUDGET_ICON_PATH = "src/main/resources/images/budget.png";
    public final static String EXPENSE_ICON_PATH = "src/main/resources/images/expense.png";
    public final static String TICKETS_ICON_PATH = "src/main/resources/images/ticket.png";
    public final static String USERS_ICON_PATH = "src/main/resources/images/user.png";
    public final static String ASSISTANT_ICON_PATH = "src/main/resources/images/assistant.png";
    public final static String ADMIN_ICON_PATH = "src/main/resources/images/admin.png";
    public final static String COMPANY_NAME = "PTDA Group 1";
    public final static String COPYRIGHT_DETAILS = "© 2024 PTDA Group 1";
    public final static String HOME_URL = "https://divi.pt";
    public final static String DEFAULT_DARK_THEME = "Dark";
    public final static String DEFAULT_LIGHT_THEME = "Light";
    public final static int MIN_PASSWORD_LENGTH = 4;
}
