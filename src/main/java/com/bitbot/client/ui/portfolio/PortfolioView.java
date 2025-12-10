package com.bitbot.client.ui.portfolio;

import com.bitbot.client.service.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Portfolio View
 * Displays asset holdings, P&L, and trading statistics
 */
public class PortfolioView extends ScrollPane {
    
    private final ThemeManager themeManager;

    public PortfolioView() {
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
        Label title = new Label("💼 Portfolio");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        // Balance Card
        VBox balanceCard = createBalanceCard();
        
        // Holdings Card
        VBox holdingsCard = createHoldingsCard();
        
        // Statistics Card
        VBox statsCard = createStatisticsCard();

        mainContent.getChildren().addAll(title, balanceCard, holdingsCard, statsCard);
        setContent(mainContent);
    }

    private VBox createBalanceCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            """, themeManager.getBgSecondary()));

        Label cardTitle = new Label("Total Balance");
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        cardTitle.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));

        Label balance = new Label("$0.00");
        balance.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        balance.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        HBox profitBox = new HBox(10);
        profitBox.setAlignment(Pos.CENTER_LEFT);
        
        Label profitLabel = new Label("All-time P&L:");
        profitLabel.setFont(Font.font("Segoe UI", 14));
        profitLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));
        
        Label profitValue = new Label("+$0.00 (0.00%)");
        profitValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        profitValue.setStyle(String.format("-fx-text-fill: %s;", ThemeManager.COLOR_SUCCESS));
        
        profitBox.getChildren().addAll(profitLabel, profitValue);

        card.getChildren().addAll(cardTitle, balance, profitBox);
        return card;
    }

    private VBox createHoldingsCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            """, themeManager.getBgSecondary()));

        Label cardTitle = new Label("Current Holdings");
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        cardTitle.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        Label placeholder = new Label("📊 No active positions\nStart trading to see your holdings here");
        placeholder.setFont(Font.font("Segoe UI", 14));
        placeholder.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-text-alignment: center;
            """, themeManager.getTextSecondary()));
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(cardTitle, placeholder);
        return card;
    }

    private VBox createStatisticsCard() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            """, themeManager.getBgSecondary()));

        Label cardTitle = new Label("Trading Statistics");
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        cardTitle.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        HBox statsGrid = new HBox(20);
        statsGrid.getChildren().addAll(
            createStatItem("Total Trades", "0"),
            createStatItem("Win Rate", "0%"),
            createStatItem("Avg Profit", "$0"),
            createStatItem("Best Trade", "$0")
        );

        card.getChildren().addAll(cardTitle, statsGrid);
        return card;
    }

    private VBox createStatItem(String label, String value) {
        VBox item = new VBox(5);
        item.setAlignment(Pos.CENTER);
        HBox.setHgrow(item, Priority.ALWAYS);

        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", 12));
        labelText.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueText.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        item.getChildren().addAll(labelText, valueText);
        return item;
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
            // Recreate content with new theme
            initializeUI();
        });
    }
}

