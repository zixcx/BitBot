package com.bitbot.client.ui.feed;

import com.bitbot.client.model.TradeDecision;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Trade Decision Card
 * Displays a single AI decision in the feed
 * 
 * Features:
 * - Collapsed view: Icon + Action + Brief reason + Time
 * - Expanded view: Full reasoning + Market snapshot
 */
public class TradeDecisionCard extends VBox {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    private final TradeDecision decision;
    private final LocalDateTime timestamp;
    private boolean expanded = false;
    
    private VBox expandedContent;

    public TradeDecisionCard(TradeDecision decision) {
        this.decision = decision;
        this.timestamp = LocalDateTime.now();
        
        initializeUI();
    }

    private void initializeUI() {
        setSpacing(8);
        setPadding(new Insets(12));
        setStyle("""
            -fx-background-color: #313244;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        
        // Hover effect
        setOnMouseEntered(e -> setStyle("""
            -fx-background-color: #3e3e52;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """));
        
        setOnMouseExited(e -> setStyle("""
            -fx-background-color: #313244;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """));
        
        // Click to expand/collapse
        setOnMouseClicked(e -> toggleExpanded());
        
        // Create collapsed view
        VBox collapsedView = createCollapsedView();
        getChildren().add(collapsedView);
        
        // Create expanded content (hidden by default)
        expandedContent = createExpandedContent();
        expandedContent.setVisible(false);
        expandedContent.setManaged(false);
        getChildren().add(expandedContent);
    }

    private VBox createCollapsedView() {
        VBox container = new VBox(5);
        
        // Header row: Icon + Action + Time
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label(decision.getActionIcon());
        icon.setStyle("-fx-font-size: 16px;");
        
        Label action = new Label(decision.getAction().toString());
        action.setStyle(String.format("""
            -fx-font-size: 13px;
            -fx-font-weight: bold;
            -fx-text-fill: %s;
            """, getActionColor()));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label time = new Label(timestamp.format(TIME_FORMATTER));
        time.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #6c7086;
            """);
        
        header.getChildren().addAll(icon, action, spacer, time);
        
        // Brief reason
        Label brief = new Label(decision.getBriefReason());
        brief.setWrapText(true);
        brief.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #cdd6f4;
            """);
        
        // Confidence badge
        Label confidence = new Label(String.format("%.0f%% confident", decision.getConfidence() * 100));
        confidence.setStyle("""
            -fx-font-size: 10px;
            -fx-text-fill: #bac2de;
            -fx-background-color: #45475a;
            -fx-padding: 2 8;
            -fx-background-radius: 4;
            """);
        
        container.getChildren().addAll(header, brief, confidence);
        return container;
    }

    private VBox createExpandedContent() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10, 0, 0, 0));
        
        // Separator
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #45475a;");
        
        // Full reasoning
        Label reasoningTitle = new Label("Analysis:");
        reasoningTitle.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #bac2de;
            -fx-font-weight: 600;
            """);
        
        Label fullReason = new Label(decision.getFullReason());
        fullReason.setWrapText(true);
        fullReason.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #cdd6f4;
            -fx-line-spacing: 2;
            """);
        
        // Price targets (if available)
        VBox priceTargets = new VBox(5);
        if (decision.getTargetPrice() != null) {
            Label target = new Label(String.format("Target: $%.2f", decision.getTargetPrice()));
            target.setStyle("-fx-font-size: 11px; -fx-text-fill: #a6e3a1;");
            priceTargets.getChildren().add(target);
        }
        if (decision.getStopLoss() != null) {
            Label stopLoss = new Label(String.format("Stop Loss: $%.2f", decision.getStopLoss()));
            stopLoss.setStyle("-fx-font-size: 11px; -fx-text-fill: #f38ba8;");
            priceTargets.getChildren().add(stopLoss);
        }
        if (decision.getTakeProfit() != null) {
            Label takeProfit = new Label(String.format("Take Profit: $%.2f", decision.getTakeProfit()));
            takeProfit.setStyle("-fx-font-size: 11px; -fx-text-fill: #89b4fa;");
            priceTargets.getChildren().add(takeProfit);
        }
        
        container.getChildren().addAll(separator, reasoningTitle, fullReason);
        if (!priceTargets.getChildren().isEmpty()) {
            container.getChildren().add(priceTargets);
        }
        
        return container;
    }

    private void toggleExpanded() {
        expanded = !expanded;
        expandedContent.setVisible(expanded);
        expandedContent.setManaged(expanded);
    }

    private String getActionColor() {
        return switch (decision.getAction()) {
            case BUY -> "#a6e3a1";
            case SELL -> "#f38ba8";
            case HOLD -> "#f9e2af";
        };
    }
}


