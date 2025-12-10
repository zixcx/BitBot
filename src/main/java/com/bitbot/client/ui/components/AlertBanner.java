package com.bitbot.client.ui.components;

import com.bitbot.client.service.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Alert Banner Component
 * Shows persistent banner notifications at the top of the screen
 */
public class AlertBanner extends HBox {
    
    private final ThemeManager themeManager;
    private BannerType type;

    public AlertBanner(String message, BannerType type, Runnable onActionClick) {
        this.themeManager = ThemeManager.getInstance();
        this.type = type;
        
        setAlignment(Pos.CENTER);
        setPadding(new Insets(15, 20, 15, 20));
        setSpacing(15);
        setStyle(String.format("""
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 0 0 2 0;
            """, getBackgroundColor(), getBorderColor()));

        // Icon
        FontIcon icon = new FontIcon(getIcon());
        icon.setIconSize(20);
        icon.setIconColor(javafx.scene.paint.Color.web(getIconColor()));

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        messageLabel.setStyle(String.format("-fx-text-fill: %s;", getTextColor()));
        messageLabel.setWrapText(true);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);

        // Action Button (optional)
        Button actionButton = null;
        if (onActionClick != null) {
            actionButton = new Button(getActionText());
            actionButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            actionButton.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-background-radius: 6;
                -fx-padding: 8 16;
                -fx-cursor: hand;
                """, getActionButtonColor()));
            actionButton.setOnAction(e -> onActionClick.run());
        }

        if (actionButton != null) {
            getChildren().addAll(icon, messageLabel, actionButton);
        } else {
            getChildren().addAll(icon, messageLabel);
        }
    }

    private String getBackgroundColor() {
        return switch (type) {
            case INFO -> themeManager.isDarkMode() ? "#1e3a5f" : "#e3f2fd";
            case WARNING -> themeManager.isDarkMode() ? "#4a3800" : "#fff8e1";
            case ERROR -> themeManager.isDarkMode() ? "#4a1616" : "#ffebee";
            case SUCCESS -> themeManager.isDarkMode() ? "#1a3d1a" : "#e8f5e9";
        };
    }

    private String getBorderColor() {
        return switch (type) {
            case INFO -> ThemeManager.COLOR_PRIMARY;
            case WARNING -> ThemeManager.COLOR_WARNING;
            case ERROR -> ThemeManager.COLOR_ERROR;
            case SUCCESS -> ThemeManager.COLOR_SUCCESS;
        };
    }

    private String getTextColor() {
        return switch (type) {
            case INFO -> themeManager.isDarkMode() ? "#90caf9" : "#1565c0";
            case WARNING -> themeManager.isDarkMode() ? "#ffb74d" : "#e65100";
            case ERROR -> themeManager.isDarkMode() ? "#ef5350" : "#c62828";
            case SUCCESS -> themeManager.isDarkMode() ? "#66bb6a" : "#2e7d32";
        };
    }

    private String getIconColor() {
        return getTextColor();
    }

    private String getActionButtonColor() {
        return switch (type) {
            case INFO -> ThemeManager.COLOR_PRIMARY;
            case WARNING -> ThemeManager.COLOR_WARNING;
            case ERROR -> ThemeManager.COLOR_ERROR;
            case SUCCESS -> ThemeManager.COLOR_SUCCESS;
        };
    }

    private FontAwesomeSolid getIcon() {
        return switch (type) {
            case INFO -> FontAwesomeSolid.INFO_CIRCLE;
            case WARNING -> FontAwesomeSolid.EXCLAMATION_TRIANGLE;
            case ERROR -> FontAwesomeSolid.EXCLAMATION_CIRCLE;
            case SUCCESS -> FontAwesomeSolid.CHECK_CIRCLE;
        };
    }

    private String getActionText() {
        return switch (type) {
            case INFO, WARNING -> "지금 완료하기";
            case ERROR -> "자세히 보기";
            case SUCCESS -> "확인";
        };
    }

    public enum BannerType {
        INFO, WARNING, ERROR, SUCCESS
    }
}

