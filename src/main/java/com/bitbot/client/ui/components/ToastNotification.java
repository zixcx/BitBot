package com.bitbot.client.ui.components;

import com.bitbot.client.service.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Toast Notification Component
 * Shows temporary notifications at the top of the screen
 */
public class ToastNotification {
    
    private final StackPane parentContainer;
    private final ThemeManager themeManager;
    private Pane toastLayer;

    public ToastNotification(StackPane parentContainer) {
        this.parentContainer = parentContainer;
        this.themeManager = ThemeManager.getInstance();
        initToastLayer();
    }
    
    private void initToastLayer() {
        // Create a dedicated toast layer that doesn't block interactions
        toastLayer = new Pane();
        toastLayer.setPickOnBounds(false);
        toastLayer.setMouseTransparent(true);
        toastLayer.prefWidthProperty().bind(parentContainer.widthProperty());
        toastLayer.prefHeightProperty().bind(parentContainer.heightProperty());
        
        // Add to parent container
        parentContainer.getChildren().add(toastLayer);
    }

    /**
     * Show error toast
     */
    public void showError(String message) {
        show(message, ToastType.ERROR);
    }

    /**
     * Show success toast
     */
    public void showSuccess(String message) {
        show(message, ToastType.SUCCESS);
    }

    /**
     * Show info toast
     */
    public void showInfo(String message) {
        show(message, ToastType.INFO);
    }

    /**
     * Show warning toast
     */
    public void showWarning(String message) {
        show(message, ToastType.WARNING);
    }

    private void show(String message, ToastType type) {
        // Create toast container - compact and fixed size
        HBox toast = new HBox(10);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(12, 16, 12, 16));
        toast.setPrefWidth(360);
        toast.setMaxWidth(360);
        toast.setMinHeight(50);
        toast.setMaxHeight(80);
        toast.setMouseTransparent(false); // Toast can receive clicks
        toast.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 6;
            -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 15, 0, 0, 8);
            """, getBackgroundColor(type)));

        // Icon
        FontIcon icon = new FontIcon(getIcon(type));
        icon.setIconSize(16);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        messageLabel.setStyle("-fx-text-fill: white;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(310);

        toast.getChildren().addAll(icon, messageLabel);

        // Calculate center position
        parentContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            double centerX = (newVal.doubleValue() - 360) / 2;
            toast.setLayoutX(centerX);
        });
        
        // Initial position
        double centerX = (parentContainer.getWidth() - 360) / 2;
        toast.setLayoutX(centerX);
        toast.setLayoutY(-100); // Start above screen

        // Add to toast layer
        toastLayer.getChildren().add(toast);

        // Slide in animation
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), toast);
        slideIn.setFromY(0);
        slideIn.setToY(120); // Slide down to visible position (20px from top + 100 offset)
        
        // Fade in
        toast.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Auto dismiss after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> dismiss(toast));

        slideIn.play();
        fadeIn.play();
        pause.play();
    }

    private void dismiss(HBox toast) {
        // Slide out up
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toast);
        slideOut.setToY(-20); // Slide back up
        
        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        // Remove from toast layer after animation
        fadeOut.setOnFinished(e -> {
            toastLayer.getChildren().remove(toast);
        });
        
        slideOut.play();
        fadeOut.play();
    }

    private String getBackgroundColor(ToastType type) {
        return switch (type) {
            case SUCCESS -> ThemeManager.COLOR_SUCCESS;
            case ERROR -> ThemeManager.COLOR_ERROR;
            case WARNING -> ThemeManager.COLOR_WARNING;
            case INFO -> ThemeManager.COLOR_PRIMARY;
        };
    }

    private FontAwesomeSolid getIcon(ToastType type) {
        return switch (type) {
            case SUCCESS -> FontAwesomeSolid.CHECK_CIRCLE;
            case ERROR -> FontAwesomeSolid.EXCLAMATION_CIRCLE;
            case WARNING -> FontAwesomeSolid.EXCLAMATION_TRIANGLE;
            case INFO -> FontAwesomeSolid.INFO_CIRCLE;
        };
    }

    private enum ToastType {
        SUCCESS, ERROR, WARNING, INFO
    }
}

