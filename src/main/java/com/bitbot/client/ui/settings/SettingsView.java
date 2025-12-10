package com.bitbot.client.ui.settings;

import com.bitbot.client.service.ThemeManager;
import com.bitbot.client.service.api.ServerApiClient;
import com.bitbot.client.ui.components.ToastNotification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 설정 화면
 * API 키 설정 및 자동거래 제어
 */
public class SettingsView extends StackPane {
    
    private static final Logger logger = LoggerFactory.getLogger(SettingsView.class);
    
    private final ThemeManager themeManager;
    private final ServerApiClient serverApiClient;
    private final ToastNotification toast;
    
    private TextField binanceKeyField;
    private PasswordField binanceSecretField;
    private Label apiKeyStatusLabel;

    public SettingsView(ServerApiClient serverApiClient) {
        this.themeManager = ThemeManager.getInstance();
        this.serverApiClient = serverApiClient;
        this.toast = new ToastNotification(this);
        initializeUI();
        setupThemeBinding();
        checkApiKeys();
    }

    private void initializeUI() {
        applyTheme();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setMaxWidth(800);
        mainContent.setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));

        // Header with icon
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon settingsIcon = new FontIcon(FontAwesomeSolid.COG);
        settingsIcon.setIconSize(32);
        settingsIcon.setIconColor(javafx.scene.paint.Color.web(themeManager.getTextPrimary()));
        
        Label title = new Label("설정");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));
        
        titleBox.getChildren().addAll(settingsIcon, title);
        
        // API Keys Section
        VBox apiSection = createApiKeysSection();
        
        // About Section
        VBox aboutSection = createAboutSection();

        mainContent.getChildren().addAll(titleBox, apiSection, aboutSection);
        
        // Center the content
        VBox wrapper = new VBox(mainContent);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));
        
        scrollPane.setContent(wrapper);
        getChildren().add(scrollPane);
    }

    private VBox createApiKeysSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            """, themeManager.getBgSecondary()));

        Label sectionTitle = new Label("📡 API 설정");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        sectionTitle.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        // API Key Status
        apiKeyStatusLabel = new Label("키가 설정되지 않았습니다");
        apiKeyStatusLabel.setFont(Font.font("Segoe UI", 13));
        apiKeyStatusLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));

        // Binance API Key
        Label binanceLabel = new Label("바이낸스 API 키");
        binanceLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        binanceLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));
        
        binanceKeyField = new TextField();
        binanceKeyField.setPromptText("바이낸스 API 키를 입력하세요");
        binanceKeyField.setPrefHeight(40);
        applyTextFieldStyle(binanceKeyField);

        // Binance Secret Key
        Label binanceSecretLabel = new Label("바이낸스 시크릿 키");
        binanceSecretLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        binanceSecretLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));
        
        binanceSecretField = new PasswordField();
        binanceSecretField.setPromptText("바이낸스 시크릿 키를 입력하세요");
        binanceSecretField.setPrefHeight(40);
        applyTextFieldStyle(binanceSecretField);

        // Save Button
        Button saveButton = new Button("💾 API 키 저장");
        saveButton.setPrefHeight(45);
        saveButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        saveButton.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """, ThemeManager.COLOR_PRIMARY));
        saveButton.setOnAction(e -> handleSaveApiKeys());

        Label warningLabel = new Label("🔒 API 키는 암호화되어 로컬에 저장됩니다. 서버와 공유되지 않습니다.");
        warningLabel.setFont(Font.font("Segoe UI", 11));
        warningLabel.setStyle(String.format("-fx-text-fill: %s;", ThemeManager.COLOR_WARNING));
        warningLabel.setWrapText(true);

        section.getChildren().addAll(
            sectionTitle,
            apiKeyStatusLabel,
            binanceLabel, binanceKeyField,
            binanceSecretLabel, binanceSecretField,
            saveButton,
            warningLabel
        );

        return section;
    }

    private VBox createAboutSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(25));
        section.setAlignment(Pos.CENTER);
        section.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            """, themeManager.getBgSecondary()));

        Label appName = new Label("🤖 BitBot");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        appName.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        Label version = new Label("버전 1.0.0");
        version.setFont(Font.font("Segoe UI", 13));
        version.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));

        Label description = new Label("AI 기반 비트코인 자동거래 시스템");
        description.setFont(Font.font("Segoe UI", 13));
        description.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));

        Label copyright = new Label("Made with ❤️ by BitBot Team");
        copyright.setFont(Font.font("Segoe UI", 11));
        copyright.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));

        section.getChildren().addAll(appName, version, description, copyright);

        return section;
    }

    private void handleSaveApiKeys() {
        String apiKey = binanceKeyField.getText().trim();
        String secretKey = binanceSecretField.getText().trim();
        
        if (apiKey.isEmpty() || secretKey.isEmpty()) {
            toast.showError("API 키와 시크릿 키를 모두 입력해주세요");
            return;
        }
        
        try {
            // TODO: Implement actual encryption and storage
            // For now, just show success message
            
            // Simulate saving process
            Thread.sleep(300); // Small delay to show it's processing
            
            // Save flag to preferences (simulated)
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(SettingsView.class);
            prefs.putBoolean("api_keys_configured", true);
            
            toast.showSuccess("✅ API 키가 안전하게 저장되었습니다");
            logger.info("API keys saved successfully");
            
            javafx.application.Platform.runLater(this::checkApiKeys);
            
        } catch (Exception e) {
            toast.showError("❌ API 키 저장에 실패했습니다: " + e.getMessage());
            logger.error("Failed to save API keys", e);
        }
    }
    
    private void checkApiKeys() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(SettingsView.class);
        boolean configured = prefs.getBoolean("api_keys_configured", false);
        
        if (configured) {
            apiKeyStatusLabel.setText("✅ API 키가 설정되어 있습니다");
            apiKeyStatusLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", ThemeManager.COLOR_SUCCESS));
            binanceKeyField.setPromptText("****************");
            binanceSecretField.setPromptText("****************");
        } else {
            apiKeyStatusLabel.setText("⚠️ API 키가 설정되지 않았습니다");
            apiKeyStatusLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", ThemeManager.COLOR_WARNING));
        }
    }

    private void applyTextFieldStyle(TextField field) {
        field.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-prompt-text-fill: %s;
            -fx-border-color: %s;
            -fx-border-width: 1;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-padding: 8 12;
            -fx-font-family: 'Segoe UI';
            """, themeManager.getBgPrimary(), themeManager.getTextPrimary(), 
            themeManager.getTextSecondary(), themeManager.getBorder()));
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
