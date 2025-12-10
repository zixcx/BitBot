package com.bitbot.client.ui.feed;

import com.bitbot.client.dto.TradingStatusDto;
import com.bitbot.client.service.ThemeManager;
import com.bitbot.client.service.api.ServerApiClient;
import com.bitbot.client.ui.components.ToastNotification;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Agent Feed Component
 * Right sidebar displaying AI trading decisions and controls
 */
public class AgentFeedView extends VBox {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentFeedView.class);
    private static final double FEED_WIDTH = 380;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private final ThemeManager themeManager;
    private final ServerApiClient serverApiClient;
    private StackPane toastContainer;
    private ToastNotification toast;
    
    private Label statusLabel;
    private FontIcon statusIcon;
    private VBox feedContainer;
    private Button startButton;
    private Button stopButton;
    private Timer refreshTimer;

    public AgentFeedView(ServerApiClient serverApiClient) {
        this.themeManager = ThemeManager.getInstance();
        this.serverApiClient = serverApiClient;
        initializeUI();
        setupThemeBinding();
        startAutoRefresh();
    }

    private void initializeUI() {
        setPrefWidth(FEED_WIDTH);
        setMinWidth(FEED_WIDTH);
        setMaxWidth(FEED_WIDTH);
        setSpacing(15);
        setPadding(new Insets(20, 15, 20, 15));
        applyTheme();

        // Toast container (StackPane for ToastNotification)
        toastContainer = new StackPane();
        toastContainer.setPickOnBounds(false);
        toastContainer.setMouseTransparent(true);
        toast = new ToastNotification(toastContainer);

        // Header
        VBox header = createHeader();
        
        // Trading Controls
        VBox controls = createTradingControls();
        
        // Feed Container with ScrollPane
        ScrollPane scrollPane = createFeedScrollPane();
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, controls, new Separator(), scrollPane, toastContainer);
    }

    private void applyTheme() {
        setStyle(String.format("-fx-background-color: %s;", themeManager.getBgSecondary()));
    }

    private void setupThemeBinding() {
        themeManager.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            applyTheme();
            updateTitleColors();
            updateStatusIconColor();
            updateFeedItemsTheme();
        });
    }

    private void updateFeedItemsTheme() {
        if (feedContainer == null) return;
        
        feedContainer.getChildren().forEach(node -> {
            if (node instanceof VBox card) {
                // Update card background and border
                card.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-background-radius: 8;
                    -fx-border-color: %s;
                    -fx-border-width: 1;
                    -fx-border-radius: 8;
                    """, themeManager.getBgPrimary(), themeManager.getBorder()));

                // Update labels inside the card
                updateCardLabelsTheme(card);
            } else if (node instanceof Label label) {
                // Placeholder label
                label.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));
            }
        });
    }

    private void updateCardLabelsTheme(VBox card) {
        // Recursively update labels within the card
        card.getChildren().forEach(node -> {
            if (node instanceof HBox hbox) {
                hbox.getChildren().forEach(child -> {
                    if (child instanceof Label label) {
                        updateLabelTheme(label);
                    }
                });
            } else if (node instanceof Label label) {
                updateLabelTheme(label);
            }
        });
    }

    private void updateLabelTheme(Label label) {
        // Skip action type labels (BUY/SELL) which have specific background colors
        if (label.getStyle().contains("-fx-background-color")) return;
        
        // Check UserData to determine style (set in createTradeLogCard)
        Object type = label.getUserData();
        if ("primary".equals(type)) {
             label.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", themeManager.getTextPrimary()));
        } else {
             // Default to secondary
             label.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", themeManager.getTextSecondary()));
        }
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        
        Label titleLabel = new Label("에이전트 피드");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        updateTitleColor(titleLabel);

        // Status indicator
        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        statusIcon = new FontIcon(FontAwesomeSolid.CIRCLE);
        statusIcon.setIconSize(12);
        statusIcon.setIconColor(javafx.scene.paint.Color.web(ThemeManager.COLOR_SUCCESS));
        
        statusLabel = new Label("모니터링 중");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        statusLabel.setStyle(String.format("-fx-text-fill: %s;", ThemeManager.COLOR_SUCCESS));
        
        statusBox.getChildren().addAll(statusIcon, statusLabel);
        header.getChildren().addAll(titleLabel, statusBox);
        
        return header;
    }

    private VBox createTradingControls() {
        VBox controls = new VBox(12);
        controls.setPadding(new Insets(10, 0, 10, 0));
        
        // Control Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        startButton = new Button("▶ 시작");
        startButton.setPrefHeight(40);
        startButton.setPrefWidth(165);
        startButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        startButton.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """, ThemeManager.COLOR_SUCCESS));
        startButton.setOnAction(e -> handleStartTrading());
        
        stopButton = new Button("■ 중지");
        stopButton.setPrefHeight(40);
        stopButton.setPrefWidth(165);
        stopButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        stopButton.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """, ThemeManager.COLOR_ERROR));
        stopButton.setOnAction(e -> handleStopTrading());
        stopButton.setDisable(true);
        
        buttonBox.getChildren().addAll(startButton, stopButton);
        controls.getChildren().add(buttonBox);
        
        return controls;
    }

    private void handleStartTrading() {
        // Disable button and show starting status
        startButton.setDisable(true);
        startButton.setText("시작 중...");
        updateStatus("시작 중", ThemeManager.COLOR_WARNING);
        
        // Check if API keys are configured
        // TODO: Check actual API keys from settings/storage
        // For now, we'll proceed with profile check
        
        // Check profile first
        serverApiClient.getUserProfile()
            .thenAccept(profile -> {
                // Profile exists, start trading
                serverApiClient.startTrading()
                    .thenAccept(result -> {
                        Platform.runLater(() -> {
                            if (result) {
                                toast.showSuccess("✅ 자동거래가 시작되었습니다");
                                startButton.setText("▶ 시작");
                                stopButton.setDisable(false);
                                updateStatus("거래 중", ThemeManager.COLOR_PRIMARY);
                                refreshTradeLogs();
                            } else {
                                toast.showError("자동거래 시작 실패");
                                startButton.setDisable(false);
                                startButton.setText("▶ 시작");
                                updateStatus("모니터링 중", ThemeManager.COLOR_SUCCESS);
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            String errorMsg = ex.getMessage();
                            // Check if error is about missing API keys
                            if (errorMsg != null && (errorMsg.contains("API") || errorMsg.contains("key"))) {
                                toast.showWarning("⚠️ 설정에서 바이낸스 API 키를 먼저 입력해주세요");
                            } else {
                                toast.showError("자동거래 시작 오류: " + errorMsg);
                            }
                            startButton.setDisable(false);
                            startButton.setText("▶ 시작");
                            updateStatus("모니터링 중", ThemeManager.COLOR_SUCCESS);
                        });
                        return null;
                    });
            })
            .exceptionally(ex -> {
                // No profile
                Platform.runLater(() -> {
                    toast.showWarning("⚠️ 먼저 투자 성향 설문조사를 완료해주세요");
                    startButton.setDisable(false);
                    startButton.setText("▶ 시작");
                    updateStatus("모니터링 중", ThemeManager.COLOR_SUCCESS);
                });
                return null;
            });
    }

    private void handleStopTrading() {
        serverApiClient.stopTrading()
            .thenAccept(result -> {
                Platform.runLater(() -> {
                    if (result) {
                        toast.showInfo("자동거래가 중지되었습니다");
                        startButton.setDisable(false);
                        stopButton.setDisable(true);
                        updateStatus("모니터링 중", ThemeManager.COLOR_SUCCESS);
                    } else {
                        toast.showError("자동거래 중지 실패");
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    toast.showError("자동거래 중지 오류: " + ex.getMessage());
                });
                return null;
            });
    }

    private void updateStatus(String text, String color) {
        statusLabel.setText(text);
        statusLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI'; -fx-font-weight: 600;", color));
        statusIcon.setIconColor(javafx.scene.paint.Color.web(color));
    }

    private void updateTitleColor(Label titleLabel) {
        titleLabel.setStyle(String.format("""
            -fx-text-fill: %s;
            -fx-font-family: 'Segoe UI';
            -fx-font-weight: bold;
            -fx-font-size: 18px;
            """, themeManager.getTextPrimary()));
    }

    private void updateTitleColors() {
        getChildren().forEach(node -> {
            if (node instanceof VBox vbox) {
                vbox.getChildren().forEach(child -> {
                    if (child instanceof Label label && label.getText().equals("에이전트 피드")) {
                        updateTitleColor(label);
                    }
                });
            }
        });
    }

    private void updateStatusIconColor() {
        if (statusIcon != null && statusLabel != null) {
            String text = statusLabel.getText();
            String color = switch (text) {
                case "모니터링 중" -> ThemeManager.COLOR_SUCCESS;
                case "분석 중" -> ThemeManager.COLOR_WARNING;
                case "거래 중" -> ThemeManager.COLOR_PRIMARY;
                case "오류" -> ThemeManager.COLOR_ERROR;
                default -> themeManager.getTextPrimary();
            };
            statusIcon.setIconColor(javafx.scene.paint.Color.web(color));
        }
    }

    private ScrollPane createFeedScrollPane() {
        feedContainer = new VBox(12);
        feedContainer.setPadding(new Insets(5));
        
        // Initial placeholder
        Label placeholderLabel = new Label("거래 내역이 없습니다");
        placeholderLabel.setFont(Font.font("Segoe UI", 13));
        placeholderLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));
        feedContainer.getChildren().add(placeholderLabel);
        
        ScrollPane scrollPane = new ScrollPane(feedContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0));
        
        return scrollPane;
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshTradingStatus();
                refreshTradeLogs();
            }
        }, 1000, 10000); // Every 10 seconds
    }

    private void refreshTradingStatus() {
        if (serverApiClient == null || !serverApiClient.isAuthenticated()) {
            return;
        }
        
        serverApiClient.getTradingStatus()
            .thenAccept(status -> {
                Platform.runLater(() -> {
                    if ("running".equals(status.getStatus())) {
                        startButton.setDisable(true);
                        stopButton.setDisable(false);
                        updateStatus("거래 중", ThemeManager.COLOR_PRIMARY);
                    } else {
                        startButton.setDisable(false);
                        stopButton.setDisable(true);
                        updateStatus("모니터링 중", ThemeManager.COLOR_SUCCESS);
                    }
                });
            })
            .exceptionally(ex -> {
                logger.debug("Failed to refresh trading status: {}", ex.getMessage());
                return null;
            });
    }

    private void refreshTradeLogs() {
        if (serverApiClient == null || !serverApiClient.isAuthenticated()) {
            return;
        }
        
        serverApiClient.getTradeLogs(20)
            .thenAccept(logs -> {
                Platform.runLater(() -> {
                    feedContainer.getChildren().clear();
                    
                    if (logs == null || logs.isEmpty()) {
                        Label placeholderLabel = new Label("거래 내역이 없습니다");
                        placeholderLabel.setFont(Font.font("Segoe UI", 13));
                        placeholderLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));
                        feedContainer.getChildren().add(placeholderLabel);
                        return;
                    }
                    
                    // Sort by createdAt (newest first)
                    logs.sort((a, b) -> {
                        try {
                            LocalDateTime dateA = LocalDateTime.parse(a.getCreatedAt());
                            LocalDateTime dateB = LocalDateTime.parse(b.getCreatedAt());
                            return dateB.compareTo(dateA); // Descending order (newest first)
                        } catch (Exception e) {
                            return 0;
                        }
                    });
                    
                    // Add trade log cards
                    logs.forEach(log -> {
                        VBox card = createTradeLogCard(log);
                        feedContainer.getChildren().add(card);
                    });
                });
            })
            .exceptionally(ex -> {
                logger.debug("Failed to refresh trade logs: {}", ex.getMessage());
                return null;
            });
    }

    private VBox createTradeLogCard(com.bitbot.client.dto.TradeLogDto log) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            -fx-border-color: %s;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            """, themeManager.getBgPrimary(), themeManager.getBorder()));

        // Header: Action Type + Time
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        String actionColor = switch (log.getActionType()) {
            case "BUY" -> ThemeManager.COLOR_SUCCESS;
            case "SELL" -> ThemeManager.COLOR_ERROR;
            default -> ThemeManager.COLOR_WARNING;
        };
        
        Label actionLabel = new Label(log.getActionType());
        actionLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        actionLabel.setStyle(String.format("""
            -fx-text-fill: white;
            -fx-background-color: %s;
            -fx-padding: 4 12;
            -fx-background-radius: 4;
            """, actionColor));
        
        Label timeLabel = new Label(formatTime(log.getCreatedAt()));
        timeLabel.setFont(Font.font("Segoe UI", 11));
        timeLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));
        timeLabel.setUserData("secondary");
        
        header.getChildren().addAll(actionLabel, timeLabel);

        // Price and Confidence
        HBox details = new HBox(15);
        details.setAlignment(Pos.CENTER_LEFT);
        
        if (log.getExecutedPrice() != null) {
            Label priceLabel = new Label(String.format("$%.2f", log.getExecutedPrice()));
            priceLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            priceLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));
            priceLabel.setUserData("primary");
            details.getChildren().add(priceLabel);
        }
        
        if (log.getConfidenceScore() != null) {
            Label confidenceLabel = new Label(String.format("신뢰도: %.1f%%", log.getConfidenceScore()));
            confidenceLabel.setFont(Font.font("Segoe UI", 12));
            confidenceLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));
            confidenceLabel.setUserData("secondary");
            details.getChildren().add(confidenceLabel);
        }

        // Reason
        if (log.getReason() != null && !log.getReason().isEmpty()) {
            Label reasonLabel = new Label(log.getReason());
            reasonLabel.setFont(Font.font("Segoe UI", 12));
            reasonLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextSecondary()));
            reasonLabel.setUserData("secondary");
            reasonLabel.setWrapText(true);
            card.getChildren().addAll(header, details, reasonLabel);
        } else {
            card.getChildren().addAll(header, details);
        }

        return card;
    }

    private String formatTime(String createdAt) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(createdAt);
            return dateTime.format(TIME_FORMATTER);
        } catch (Exception e) {
            return createdAt;
        }
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }
}
