package com.bitbot.client.ui.auth;

import com.bitbot.client.service.ThemeManager;
import com.bitbot.client.service.api.ServerApiClient;
import com.bitbot.client.ui.components.ToastNotification;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Login Screen
 * User authentication interface
 */
public class LoginView extends StackPane {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginView.class);
    
    private final ServerApiClient serverApiClient;
    private final ThemeManager themeManager;
    private final ToastNotification toast;
    
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;
    
    private LoginListener listener;

    public LoginView(ServerApiClient serverApiClient) {
        this.serverApiClient = serverApiClient;
        this.themeManager = ThemeManager.getInstance();
        this.toast = new ToastNotification(this);
        
        initializeUI();
        setupThemeBinding();
    }

    private void initializeUI() {
        // Main container - taller and narrower
        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(60));
        container.setMaxWidth(420);
        container.setMinHeight(650);
        
        // Logo/Title
        VBox header = createHeader();
        
        // Login form
        VBox form = createLoginForm();
        
        // Footer
        HBox footer = createFooter();
        
        container.getChildren().addAll(header, form, footer);
        
        // Center the container
        StackPane.setAlignment(container, Pos.CENTER);
        getChildren().add(container);
        
        applyTheme();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        Label logo = new Label("🤖 BitBot");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        
        Label subtitle = new Label("AI 기반 비트코인 거래 보조 시스템");
        subtitle.setFont(Font.font("Segoe UI", 14));
        
        header.getChildren().addAll(logo, subtitle);
        
        return header;
    }

    private VBox createLoginForm() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        
        // Email field
        Label emailLabel = new Label("이메일");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        
        emailField = new TextField();
        emailField.setPromptText("user@example.com");
        emailField.setPrefHeight(40);
        emailField.setMaxWidth(350);
        emailField.setFont(Font.font("Segoe UI", 13));
        
        // Password field
        Label passwordLabel = new Label("비밀번호");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        
        passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호를 입력하세요");
        passwordField.setPrefHeight(40);
        passwordField.setMaxWidth(350);
        passwordField.setFont(Font.font("Segoe UI", 13));
        
        // Login button
        loginButton = new Button("로그인");
        loginButton.setPrefWidth(350);
        loginButton.setPrefHeight(45);
        loginButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        loginButton.setOnAction(e -> handleLogin());
        
        // Register button
        registerButton = new Button("계정 만들기");
        registerButton.setPrefWidth(350);
        registerButton.setPrefHeight(45);
        registerButton.setFont(Font.font("Segoe UI", 14));
        registerButton.setOnAction(e -> handleRegister());
        
        // Enter key support
        passwordField.setOnAction(e -> handleLogin());
        
        form.getChildren().addAll(
            emailLabel, emailField,
            passwordLabel, passwordField,
            loginButton,
            registerButton
        );
        
        return form;
    }

    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER);
        
        Label infoLabel = new Label("Made with ❤️ by BitBot Team | v1.0.0");
        infoLabel.setFont(Font.font("Segoe UI", 11));
        
        footer.getChildren().add(infoLabel);
        
        return footer;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            toast.showError("이메일과 비밀번호를 입력해주세요");
            return;
        }
        
        if (!email.contains("@")) {
            toast.showError("올바른 이메일 주소를 입력해주세요");
            return;
        }
        
        // Disable buttons
        loginButton.setDisable(true);
        registerButton.setDisable(true);
        loginButton.setText("로그인 중...");
        
        // Call API
        serverApiClient.login(email, password)
            .thenAccept(sessionToken -> {
                javafx.application.Platform.runLater(() -> {
                    logger.info("Login successful");
                    toast.showSuccess("로그인 성공!");
                    if (listener != null) {
                        // Small delay to show success message
                        PauseTransition pause = new PauseTransition(Duration.millis(800));
                        pause.setOnFinished(e -> listener.onLoginSuccess(email, sessionToken));
                        pause.play();
                    }
                });
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    String errorMsg = ex.getMessage();
                    if (errorMsg.contains("401") || errorMsg.contains("Unauthorized")) {
                        toast.showError("이메일 또는 비밀번호가 올바르지 않습니다");
                    } else if (errorMsg.contains("Connection refused") || errorMsg.contains("ConnectException")) {
                        toast.showError("서버에 연결할 수 없습니다. 인터넷 연결을 확인해주세요.");
                    } else {
                        toast.showError("로그인에 실패했습니다. 다시 시도해주세요.");
                    }
                    logger.error("Login failed: {}", errorMsg);
                    loginButton.setDisable(false);
                    registerButton.setDisable(false);
                    loginButton.setText("로그인");
                });
                return null;
            });
    }

    private void handleRegister() {
        if (listener != null) {
            listener.onRegisterRequest();
        }
    }

    private void applyTheme() {
        String bgPrimary = themeManager.getBgPrimary();
        String bgSecondary = themeManager.getBgSecondary();
        String bgTertiary = themeManager.getBgTertiary();
        String textPrimary = themeManager.getTextPrimary();
        String textSecondary = themeManager.getTextSecondary();
        String border = themeManager.getBorder();
        
        // Background
        setStyle(String.format("-fx-background-color: %s;", bgPrimary));
        
        // Update all labels to use theme colors
        getChildren().forEach(node -> updateNodeColors(node, textPrimary, textSecondary));
        
        // Text fields style
        String textFieldStyle = String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-prompt-text-fill: %s;
            -fx-border-color: %s;
            -fx-border-width: 1;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-padding: 8 12;
            -fx-font-family: 'Segoe UI';
            """, bgSecondary, textPrimary, textSecondary, border);
        
        emailField.setStyle(textFieldStyle);
        passwordField.setStyle(textFieldStyle);
        
        // Login button (primary)
        loginButton.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-font-weight: bold;
            -fx-font-family: 'Segoe UI';
            """, ThemeManager.COLOR_PRIMARY));
        
        // Register button (secondary)
        registerButton.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            -fx-font-family: 'Segoe UI';
            """, bgPrimary, textPrimary, border));
    }
    
    private void updateNodeColors(javafx.scene.Node node, String textPrimary, String textSecondary) {
        if (node instanceof StackPane stackPane) {
            stackPane.getChildren().forEach(child -> updateNodeColors(child, textPrimary, textSecondary));
        } else if (node instanceof VBox vbox) {
            vbox.getChildren().forEach(child -> updateNodeColors(child, textPrimary, textSecondary));
        } else if (node instanceof HBox hbox) {
            hbox.getChildren().forEach(child -> updateNodeColors(child, textPrimary, textSecondary));
        } else if (node instanceof Label label) {
            // Don't override emoji labels
            if (!label.getText().contains("❤️")) {
                label.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", textPrimary));
            }
        }
    }

    private void setupThemeBinding() {
        themeManager.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            applyTheme();
        });
    }

    public void setListener(LoginListener listener) {
        this.listener = listener;
    }

    public interface LoginListener {
        void onLoginSuccess(String email, String sessionToken);
        void onRegisterRequest();
    }
}

