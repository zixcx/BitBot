package com.bitbot.client.ui.auth;

import com.bitbot.client.service.ThemeManager;
import com.bitbot.client.service.api.ServerApiClient;
import com.bitbot.client.ui.components.ToastNotification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registration Screen
 * New user registration interface
 */
public class RegisterView extends StackPane {
    
    private static final Logger logger = LoggerFactory.getLogger(RegisterView.class);
    
    private final ServerApiClient serverApiClient;
    private final ThemeManager themeManager;
    private final ToastNotification toast;
    
    private TextField usernameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField binanceApiKeyField;
    private TextField binanceSecretKeyField;
    private Button registerButton;
    private Button backToLoginButton;
    
    private RegisterListener listener;

    public RegisterView(ServerApiClient serverApiClient) {
        this.serverApiClient = serverApiClient;
        this.themeManager = ThemeManager.getInstance();
        this.toast = new ToastNotification(this);
        
        initializeUI();
        setupThemeBinding();
    }

    private void initializeUI() {
        // Main container with scroll - taller and narrower
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));
        container.setMaxWidth(420);
        container.setMinHeight(750);
        
        // Header
        VBox header = createHeader();
        
        // Registration form
        VBox form = createRegistrationForm();
        
        // Footer
        HBox footer = createFooter();
        
        container.getChildren().addAll(header, form, footer);
        scrollPane.setContent(container);
        
        // Center the scrollpane
        StackPane wrapper = new StackPane(scrollPane);
        wrapper.setAlignment(Pos.CENTER);
        
        getChildren().add(wrapper);
        
        applyTheme();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        Label title = new Label("계정 만들기");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        
        Label subtitle = new Label("BitBot에 가입하고 AI와 함께 거래를 시작하세요");
        subtitle.setFont(Font.font("Segoe UI", 13));
        
        header.getChildren().addAll(title, subtitle);
        
        return header;
    }

    private VBox createRegistrationForm() {
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        
        // Username
        Label usernameLabel = new Label("사용자 이름");
        usernameLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        usernameField = new TextField();
        usernameField.setPromptText("표시될 이름");
        usernameField.setPrefHeight(38);
        usernameField.setMaxWidth(350);
        
        // Email
        Label emailLabel = new Label("이메일 *");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        emailField = new TextField();
        emailField.setPromptText("user@example.com");
        emailField.setPrefHeight(38);
        emailField.setMaxWidth(350);
        
        // Password
        Label passwordLabel = new Label("비밀번호 *");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        passwordField = new PasswordField();
        passwordField.setPromptText("최소 8자 이상");
        passwordField.setPrefHeight(38);
        passwordField.setMaxWidth(350);
        
        // Confirm Password
        Label confirmPasswordLabel = new Label("비밀번호 확인 *");
        confirmPasswordLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("비밀번호를 다시 입력하세요");
        confirmPasswordField.setPrefHeight(38);
        confirmPasswordField.setMaxWidth(350);
        
        // Binance API Keys (optional)
        Label apiKeysLabel = new Label("바이낸스 API 키 (선택사항)");
        apiKeysLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        
        binanceApiKeyField = new TextField();
        binanceApiKeyField.setPromptText("바이낸스 API 키");
        binanceApiKeyField.setPrefHeight(38);
        binanceApiKeyField.setMaxWidth(350);
        
        binanceSecretKeyField = new TextField();
        binanceSecretKeyField.setPromptText("바이낸스 시크릿 키");
        binanceSecretKeyField.setPrefHeight(38);
        binanceSecretKeyField.setMaxWidth(350);
        
        Label apiNote = new Label("💡 API 키는 나중에 설정에서 추가할 수 있습니다");
        apiNote.setFont(Font.font("Segoe UI", 11));
        
        // Register button
        registerButton = new Button("계정 만들기");
        registerButton.setPrefWidth(350);
        registerButton.setPrefHeight(45);
        registerButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        registerButton.setOnAction(e -> handleRegister());
        
        // Back button
        backToLoginButton = new Button("← 로그인으로 돌아가기");
        backToLoginButton.setPrefWidth(350);
        backToLoginButton.setPrefHeight(40);
        backToLoginButton.setFont(Font.font("Segoe UI", 13));
        backToLoginButton.setOnAction(e -> {
            if (listener != null) {
                listener.onBackToLogin();
            }
        });
        
        form.getChildren().addAll(
            usernameLabel, usernameField,
            emailLabel, emailField,
            passwordLabel, passwordField,
            confirmPasswordLabel, confirmPasswordField,
            new Separator(),
            apiKeysLabel,
            binanceApiKeyField,
            binanceSecretKeyField,
            apiNote,
            registerButton,
            backToLoginButton
        );
        
        return form;
    }

    private HBox createFooter() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER);
        
        Label infoLabel = new Label("By registering, you agree to our Terms of Service");
        infoLabel.setFont(Font.font("System", 10));
        
        footer.getChildren().add(infoLabel);
        
        return footer;
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String binanceApiKey = binanceApiKeyField.getText().trim();
        String binanceSecretKey = binanceSecretKeyField.getText().trim();
        
        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            toast.showError("이메일과 비밀번호는 필수입니다");
            return;
        }
        
        if (!email.contains("@")) {
            toast.showError("올바른 이메일 주소를 입력해주세요");
            return;
        }
        
        if (password.length() < 8) {
            toast.showError("비밀번호는 최소 8자 이상이어야 합니다");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            toast.showError("비밀번호가 일치하지 않습니다");
            return;
        }
        
        // Disable buttons
        registerButton.setDisable(true);
        backToLoginButton.setDisable(true);
        registerButton.setText("계정 생성 중...");
        
        // Call registration API
        serverApiClient.register(email, 
                                username.isEmpty() ? email.split("@")[0] : username, 
                                password, 
                                binanceApiKey.isEmpty() ? null : binanceApiKey, 
                                binanceSecretKey.isEmpty() ? null : binanceSecretKey)
            .thenAccept(success -> {
                javafx.application.Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    backToLoginButton.setDisable(false);
                    registerButton.setText("계정 만들기");
                    
                    if (success) {
                        logger.info("Registration successful for: {}", email);
                        toast.showSuccess("회원가입 성공! 로그인해주세요.");
                        
                        // Wait 2 seconds then go to login
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                javafx.application.Platform.runLater(() -> {
                                    if (listener != null) {
                                        listener.onRegisterSuccess(email);
                                    }
                                });
                            } catch (InterruptedException e) {
                                logger.error("Thread interrupted", e);
                            }
                        }).start();
                    } else {
                        logger.error("Registration failed for: {}", email);
                        toast.showError("회원가입 실패. 다시 시도해주세요.");
                    }
                });
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    backToLoginButton.setDisable(false);
                    registerButton.setText("계정 만들기");
                    logger.error("Registration error", ex);
                    toast.showError("회원가입 오류: " + ex.getMessage());
                });
                return null;
            });
    }

    private void applyTheme() {
        String bgPrimary = themeManager.getBgPrimary();
        String bgSecondary = themeManager.getBgSecondary();
        String textPrimary = themeManager.getTextPrimary();
        String textSecondary = themeManager.getTextSecondary();
        String border = themeManager.getBorder();
        
        setStyle(String.format("-fx-background-color: %s;", bgPrimary));
        
        // Update all labels
        getChildren().forEach(node -> updateNodeColors(node, textPrimary, textSecondary));
        
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
        
        usernameField.setStyle(textFieldStyle);
        emailField.setStyle(textFieldStyle);
        passwordField.setStyle(textFieldStyle);
        confirmPasswordField.setStyle(textFieldStyle);
        binanceApiKeyField.setStyle(textFieldStyle);
        binanceSecretKeyField.setStyle(textFieldStyle);
        
        registerButton.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-font-family: 'Segoe UI';
            """, ThemeManager.COLOR_SUCCESS));
        
        backToLoginButton.setStyle(String.format("""
            -fx-background-color: transparent;
            -fx-text-fill: %s;
            -fx-cursor: hand;
            -fx-font-family: 'Segoe UI';
            """, textSecondary));
    }
    
    private void updateNodeColors(javafx.scene.Node node, String textPrimary, String textSecondary) {
        if (node instanceof StackPane stackPane) {
            stackPane.getChildren().forEach(child -> updateNodeColors(child, textPrimary, textSecondary));
        } else if (node instanceof ScrollPane scrollPane) {
            updateNodeColors(scrollPane.getContent(), textPrimary, textSecondary);
        } else if (node instanceof VBox vbox) {
            vbox.getChildren().forEach(child -> updateNodeColors(child, textPrimary, textSecondary));
        } else if (node instanceof HBox hbox) {
            hbox.getChildren().forEach(child -> updateNodeColors(child, textPrimary, textSecondary));
        } else if (node instanceof Label label) {
            if (!label.getText().contains("💡")) {
                label.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", textPrimary));
            }
        }
    }

    private void setupThemeBinding() {
        themeManager.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            applyTheme();
        });
    }

    public void setListener(RegisterListener listener) {
        this.listener = listener;
    }

    public interface RegisterListener {
        void onRegisterSuccess(String email);
        void onBackToLogin();
    }
}

