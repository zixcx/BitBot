package com.bitbot.client.service;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Theme Manager
 * Manages application theme (Light/Dark mode)
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    
    private final BooleanProperty darkModeProperty;
    
    // Light Theme Colors
    public static final String LIGHT_BG_PRIMARY = "#ffffff";
    public static final String LIGHT_BG_SECONDARY = "#f5f5f5";
    public static final String LIGHT_BG_TERTIARY = "#e0e0e0";
    public static final String LIGHT_TEXT_PRIMARY = "#1a1a1a";
    public static final String LIGHT_TEXT_SECONDARY = "#666666";
    public static final String LIGHT_BORDER = "#cccccc";
    
    // Dark Theme Colors (Catppuccin Mocha)
    public static final String DARK_BG_PRIMARY = "#1e1e2e";
    public static final String DARK_BG_SECONDARY = "#181825";
    public static final String DARK_BG_TERTIARY = "#313244";
    public static final String DARK_TEXT_PRIMARY = "#cdd6f4";
    public static final String DARK_TEXT_SECONDARY = "#bac2de";
    public static final String DARK_BORDER = "#45475a";
    
    // Accent Colors (same for both themes, vibrant for visibility)
    public static final String COLOR_PRIMARY = "#5B9FFF";      // Brighter blue
    public static final String COLOR_SUCCESS = "#4FBF4F";       // Brighter green
    public static final String COLOR_WARNING = "#FFB84D";       // Brighter yellow/orange
    public static final String COLOR_ERROR = "#FF6B6B";         // Brighter red

    private ThemeManager() {
        this.darkModeProperty = new SimpleBooleanProperty(false); // Start with light mode
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public BooleanProperty darkModeProperty() {
        return darkModeProperty;
    }

    public boolean isDarkMode() {
        return darkModeProperty.get();
    }

    public void setDarkMode(boolean darkMode) {
        darkModeProperty.set(darkMode);
    }

    public void toggleTheme() {
        setDarkMode(!isDarkMode());
    }

    // Helper methods to get current theme colors
    public String getBgPrimary() {
        return isDarkMode() ? DARK_BG_PRIMARY : LIGHT_BG_PRIMARY;
    }

    public String getBgSecondary() {
        return isDarkMode() ? DARK_BG_SECONDARY : LIGHT_BG_SECONDARY;
    }

    public String getBgTertiary() {
        return isDarkMode() ? DARK_BG_TERTIARY : LIGHT_BG_TERTIARY;
    }

    public String getTextPrimary() {
        return isDarkMode() ? DARK_TEXT_PRIMARY : LIGHT_TEXT_PRIMARY;
    }

    public String getTextSecondary() {
        return isDarkMode() ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY;
    }

    public String getBorder() {
        return isDarkMode() ? DARK_BORDER : LIGHT_BORDER;
    }
}

