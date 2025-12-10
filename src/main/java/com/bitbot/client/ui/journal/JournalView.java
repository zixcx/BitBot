package com.bitbot.client.ui.journal;

import com.bitbot.client.service.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Journal View
 * Displays trading history and past decisions
 */
public class JournalView extends ScrollPane {
    
    private final ThemeManager themeManager;

    public JournalView() {
        this.themeManager = ThemeManager.getInstance();
        initializeUI();
        setupThemeBinding();
    }

    private void initializeUI() {
        setFitToWidth(true);
        applyTheme();

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));

        // Header
        Label title = new Label("📖 Trading Journal");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        Label subtitle = new Label("Review your past trading decisions and performance");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));

        // Placeholder
        VBox placeholderCard = createPlaceholderCard();

        mainContent.getChildren().addAll(title, subtitle, placeholderCard);
        setContent(mainContent);
    }

    private VBox createPlaceholderCard() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(60));
        card.setAlignment(Pos.CENTER);
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            """, themeManager.getBgSecondary()));

        Label icon = new Label("📝");
        icon.setFont(Font.font(48));

        Label message = new Label("No trading history yet");
        message.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        message.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        Label description = new Label("Start auto trading to see your trade history here.\nAll AI decisions and executions will be logged automatically.");
        description.setFont(Font.font("Segoe UI", 14));
        description.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-text-alignment: center;
            """, themeManager.getTextSecondary()));
        description.setAlignment(Pos.CENTER);
        description.setWrapText(true);
        description.setMaxWidth(400);

        card.getChildren().addAll(icon, message, description);
        return card;
    }

    private void applyTheme() {
        setStyle(String.format("""
            -fx-background: %s;
            -fx-background-color: %s;
            """, themeManager.getBgPrimary(), themeManager.getBgPrimary()));
    }

    private void setupThemeBinding() {
        themeManager.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            applyTheme();
            initializeUI();
        });
    }
}

