package com.bitbot.client.ui.dashboard;

import com.bitbot.client.dto.ChartDataDto;
import com.bitbot.client.model.Candle;
import com.bitbot.client.service.ThemeManager;
import com.bitbot.client.service.api.ServerApiClient;
import com.bitbot.client.ui.components.AlertBanner;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard Component
 * Main center panel displaying:
 * - Market header (BTC price, 24h change)
 * - TradingView Widget (WebView)
 * - Technical indicators (RSI, MACD, Bollinger Bands)
 * - Order book
 */
public class DashboardView extends ScrollPane {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardView.class);
    
    private final ThemeManager themeManager;
    private final ServerApiClient serverApiClient;
    private Label priceLabel;
    private Label changeLabel;
    private WebView chartWebView;
    private HBox indicatorsContainer;
    private VBox mainContent;
    private AlertBanner questionnaireBanner;
    private QuestionnaireActionListener questionnaireListener;
    private Timeline marketDataTimeline;
    
    // Wallet info labels
    private Label totalBalanceLabel;
    private Label btcHoldingLabel;
    private Label profitLossLabel;
    private Label walletTitleLabel;

    public DashboardView(ServerApiClient serverApiClient) {
        this.themeManager = ThemeManager.getInstance();
        this.serverApiClient = serverApiClient;
        initializeUI();
        setupThemeBinding();
        checkQuestionnaire();
        startMarketDataRefresh();
    }

    private void initializeUI() {
        setFitToWidth(true);
        applyTheme();

        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        applyContentTheme(mainContent);

        // Wallet Info section (at top)
        VBox walletInfo = createWalletInfo();
        
        // Header section (BTC Price)
        VBox header = createHeader();
        
        // Chart section (TradingView Widget)
        VBox chartContainer = createChartContainer();
        
        // Indicators section
        indicatorsContainer = createIndicatorsContainer();

        mainContent.getChildren().addAll(walletInfo, header, chartContainer, indicatorsContainer);
        setContent(mainContent);
        
        // Load wallet info
        loadWalletInfo();
    }
    
    private void startMarketDataRefresh() {
        // Refresh immediately
        refreshMarketData();
        
        // Schedule refresh every 1 second
        marketDataTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshMarketData()));
        marketDataTimeline.setCycleCount(Timeline.INDEFINITE);
        marketDataTimeline.play();
    }
    
    private void stopMarketDataRefresh() {
        if (marketDataTimeline != null) {
            marketDataTimeline.stop();
        }
    }
    
    private void refreshMarketData() {
        String symbol = "BTCUSDT";
        
        // Fetch Price
        serverApiClient.getMarketPrice(symbol)
            .thenAccept(priceDto -> {
                Platform.runLater(() -> {
                    if (priceDto != null) {
                        // Fetch 24h stats to get change percent
                        serverApiClient.getMarketStats(symbol)
                            .thenAccept(statsDto -> {
                                Platform.runLater(() -> {
                                    if (statsDto != null) {
                                        updatePrice(priceDto.getPrice(), statsDto.getPriceChangePercent());
                                    } else {
                                        updatePrice(priceDto.getPrice(), 0.0);
                                    }
                                });
                            });
                    }
                });
            })
            .exceptionally(ex -> {
                logger.error("Failed to refresh price: {}", ex.getMessage());
                return null;
            });
            
        // Indicators only update (Chart is handled by WebView)
        serverApiClient.getChartData(symbol, "1h", 100)
            .thenAccept(chartDataDtos -> {
                Platform.runLater(() -> {
                    if (chartDataDtos != null && !chartDataDtos.isEmpty()) {
                        updateIndicators(chartDataDtos.get(chartDataDtos.size() - 1));
                    }
                });
            })
            .exceptionally(ex -> {
                logger.error("Failed to refresh chart data: {}", ex.getMessage());
                return null;
            });
            
        // Refresh wallet info if authenticated
        if (serverApiClient.isAuthenticated()) {
            loadWalletInfo();
        }
    }
    
    private List<Candle> convertToCandles(List<ChartDataDto> dtos) {
        return dtos.stream()
            .filter(dto -> dto.getOpen() != null && dto.getOpen() > 0 &&
                          dto.getHigh() != null && dto.getHigh() > 0 &&
                          dto.getLow() != null && dto.getLow() > 0 &&
                          dto.getClose() != null && dto.getClose() > 0)
            .map(dto -> new Candle(
                dto.getTimestamp(),
                dto.getOpen(),
                dto.getHigh(),
                dto.getLow(),
                dto.getClose(),
                dto.getVolume() != null ? dto.getVolume() : 0.0
            ))
            .collect(Collectors.toList());
    }
    
    private void updateIndicators(ChartDataDto latestData) {
        if (indicatorsContainer.getChildren().size() >= 3) {
            // Update RSI
            VBox rsiCard = (VBox) indicatorsContainer.getChildren().get(0);
            Label rsiValueLabel = (Label) rsiCard.getChildren().get(1);
            rsiValueLabel.setText(String.format("%.2f", latestData.getRsi() != null ? latestData.getRsi() : 0.0));
            
            // Update MACD
            VBox macdCard = (VBox) indicatorsContainer.getChildren().get(1);
            Label macdValueLabel = (Label) macdCard.getChildren().get(1);
            macdValueLabel.setText(String.format("%.2f", latestData.getMacd() != null ? latestData.getMacd() : 0.0));
            
            // Update BB
            VBox bbCard = (VBox) indicatorsContainer.getChildren().get(2);
            Label bbValueLabel = (Label) bbCard.getChildren().get(1);
            double upper = latestData.getBollingerUpper() != null ? latestData.getBollingerUpper() : 0.0;
            double lower = latestData.getBollingerLower() != null ? latestData.getBollingerLower() : 0.0;
            double close = latestData.getClose();
            double pctB = (upper - lower) != 0 ? (close - lower) / (upper - lower) : 0.0;
            
            bbValueLabel.setText(String.format("%.2f", pctB));
        }
    }
    
    private void checkQuestionnaire() {
        if (serverApiClient == null || !serverApiClient.isAuthenticated()) {
            return;
        }
        
        serverApiClient.getUserProfile()
            .thenAccept(profile -> {
                Platform.runLater(() -> {
                    if (questionnaireBanner != null && mainContent.getChildren().contains(questionnaireBanner)) {
                        mainContent.getChildren().remove(questionnaireBanner);
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    if (questionnaireBanner == null) {
                        questionnaireBanner = new AlertBanner(
                            "자동거래를 시작하려면 먼저 투자 성향 설문조사를 완료해주세요",
                            AlertBanner.BannerType.WARNING,
                            () -> {
                                if (questionnaireListener != null) {
                                    questionnaireListener.onQuestionnaireRequested();
                                }
                            }
                        );
                        mainContent.getChildren().add(0, questionnaireBanner);
                    }
                });
                return null;
            });
    }
    
    private VBox createWalletInfo() {
        VBox walletSection = new VBox(15);
        walletSection.setPadding(new Insets(20));
        walletSection.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            -fx-border-color: %s;
            -fx-border-width: 1;
            -fx-border-radius: 12;
            """, themeManager.getBgSecondary(), themeManager.getBorder()));

        // Title
        walletTitleLabel = new Label("💰 내 지갑");
        walletTitleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        walletTitleLabel.setStyle(String.format("-fx-text-fill: %s;", themeManager.getTextPrimary()));

        // Wallet stats
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // Total Balance
        VBox totalBalanceBox = new VBox(5);
        totalBalanceBox.setAlignment(Pos.CENTER_LEFT);
        Label totalBalanceTitle = new Label("총 자산");
        totalBalanceTitle.setFont(Font.font("Segoe UI", 12));
        totalBalanceTitle.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", themeManager.getTextSecondary()));
        totalBalanceLabel = new Label("$0.00");
        totalBalanceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        totalBalanceLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", themeManager.getTextPrimary()));
        totalBalanceBox.getChildren().addAll(totalBalanceTitle, totalBalanceLabel);

        // BTC Holding
        VBox btcHoldingBox = new VBox(5);
        btcHoldingBox.setAlignment(Pos.CENTER_LEFT);
        Label btcHoldingTitle = new Label("보유 BTC");
        btcHoldingTitle.setFont(Font.font("Segoe UI", 12));
        btcHoldingTitle.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", themeManager.getTextSecondary()));
        btcHoldingLabel = new Label("0.00000000 BTC");
        btcHoldingLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        btcHoldingLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", themeManager.getTextPrimary()));
        btcHoldingBox.getChildren().addAll(btcHoldingTitle, btcHoldingLabel);

        // Profit/Loss
        VBox profitLossBox = new VBox(5);
        profitLossBox.setAlignment(Pos.CENTER_LEFT);
        Label profitLossTitle = new Label("수익/손실");
        profitLossTitle.setFont(Font.font("Segoe UI", 12));
        profitLossTitle.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", themeManager.getTextSecondary()));
        profitLossLabel = new Label("$0.00 (0.00%)");
        profitLossLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        profitLossLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI';", ThemeManager.COLOR_SUCCESS));
        profitLossBox.getChildren().addAll(profitLossTitle, profitLossLabel);

        statsBox.getChildren().addAll(totalBalanceBox, btcHoldingBox, profitLossBox);
        walletSection.getChildren().addAll(walletTitleLabel, statsBox);

        return walletSection;
    }

    private void loadWalletInfo() {
        if (serverApiClient == null || !serverApiClient.isAuthenticated()) {
            return;
        }

        serverApiClient.getAccountInfo()
            .thenAccept(account -> {
                Platform.runLater(() -> {
                    if (account != null) {
                        totalBalanceLabel.setText(String.format("$%.2f", account.getTotalBalance()));
                        btcHoldingLabel.setText(String.format("%.8f BTC", account.getBtcHolding()));
                        
                        double profitLoss = account.getTotalProfitLoss() != null ? account.getTotalProfitLoss() : 0.0;
                        double profitLossPercent = account.getProfitLossPercent() != null ? account.getProfitLossPercent() : 0.0;
                        String profitLossColor = profitLoss >= 0 ? ThemeManager.COLOR_SUCCESS : ThemeManager.COLOR_ERROR;
                        String profitLossSign = profitLoss >= 0 ? "+" : "";
                        
                        profitLossLabel.setText(String.format("%s$%.2f (%.2f%%)", profitLossSign, profitLoss, profitLossPercent));
                        profitLossLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI'; -fx-font-weight: 600;", profitLossColor));
                    }
                });
            })
            .exceptionally(ex -> {
                logger.debug("Failed to load wallet info: {}", ex.getMessage());
                return null;
            });
    }

    private void applyTheme() {
        setStyle(String.format("""
            -fx-background: %s;
            -fx-background-color: %s;
            """, themeManager.getBgPrimary(), themeManager.getBgPrimary()));
    }

    private void applyContentTheme(VBox content) {
        content.setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));
    }

    private void setupThemeBinding() {
        themeManager.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            applyTheme();
            VBox content = (VBox) getContent();
            if (content != null) {
                applyContentTheme(content);
                // Update all children
                content.getChildren().forEach(node -> {
                    if (node instanceof VBox vbox) {
                        updateVBoxTheme(vbox);
                    } else if (node instanceof HBox hbox) {
                        updateHBoxTheme(hbox);
                    }
                });
            }
            
            // Reload TradingView widget to update theme
            loadTradingViewWidget();
            
            // Update wallet info colors
            if (walletTitleLabel != null) {
                walletTitleLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;", themeManager.getTextPrimary()));
            }
            if (totalBalanceLabel != null) {
                totalBalanceLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;", themeManager.getTextPrimary()));
            }
            if (btcHoldingLabel != null) {
                btcHoldingLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-family: 'Segoe UI'; -fx-font-weight: 600;", themeManager.getTextPrimary()));
            }
            
            // Force update price label colors
            if (priceLabel != null) {
                priceLabel.setStyle(String.format("""
                    -fx-font-size: 36px;
                    -fx-text-fill: %s;
                    -fx-font-weight: bold;
                    -fx-font-family: 'Segoe UI';
                    """, themeManager.getTextPrimary()));
            }
        });
    }

    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER_LEFT);

        Label symbolLabel = new Label("BTCUSDT");
        symbolLabel.setStyle(String.format("""
            -fx-font-size: 14px;
            -fx-text-fill: %s;
            -fx-font-weight: 500;
            -fx-font-family: 'Segoe UI';
            """, themeManager.getTextSecondary()));

        HBox priceRow = new HBox(15);
        priceRow.setAlignment(Pos.CENTER_LEFT);

        priceLabel = new Label("$0.00");
        priceLabel.setStyle(String.format("""
            -fx-font-size: 36px;
            -fx-text-fill: %s;
            -fx-font-weight: bold;
            -fx-font-family: 'Segoe UI';
            """, themeManager.getTextPrimary()));

        changeLabel = new Label("+0.00%");
        changeLabel.setStyle(String.format("""
            -fx-font-size: 18px;
            -fx-text-fill: %s;
            -fx-font-weight: 600;
            -fx-padding: 5 12;
            -fx-background-color: rgba(166, 227, 161, 0.1);
            -fx-background-radius: 6;
            -fx-font-family: 'Segoe UI';
            """, ThemeManager.COLOR_SUCCESS));

        priceRow.getChildren().addAll(priceLabel, changeLabel);
        header.getChildren().addAll(symbolLabel, priceRow);

        return header;
    }

    private VBox createChartContainer() {
        VBox container = new VBox();
        container.setPrefHeight(640); // Increased height for better visibility
        container.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            -fx-padding: 20;
            """, themeManager.getBgSecondary()));

        // Create WebView for TradingView Widget
        chartWebView = new WebView();
        chartWebView.setPrefHeight(600);
        chartWebView.setMinHeight(600);
        // Removed VBox.setVgrow to prevent layout issues within ScrollPane
        
        // Load chart initially
        loadTradingViewWidget();
        
        container.getChildren().add(chartWebView);
        
        return container;
    }
    
    private void loadTradingViewWidget() {
        String theme = themeManager.isDarkMode() ? "dark" : "light";
        // Determine background color based on theme (using hex codes directly for HTML)
        // Dark: #1e1e2e (Catppuccin Mocha Base), Light: #eff1f5 (Catppuccin Latte Base)
        String backgroundColor = themeManager.isDarkMode() ? "#1e1e2e" : "#eff1f5"; 
        
        String content = String.format("""
            <!DOCTYPE html>
            <html style="height: 100%%;">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { margin: 0; padding: 0; background-color: %s; overflow: hidden; height: 100%%; }
                    .tradingview-widget-container { height: 100%% !important; width: 100%% !important; }
                    iframe { height: 100%% !important; width: 100%% !important; }
                </style>
            </head>
            <body>
                <div class="tradingview-widget-container">
                  <div id="tradingview_widget"></div>
                  <script type="text/javascript" src="https://s3.tradingview.com/tv.js"></script>
                  <script type="text/javascript">
                  new TradingView.widget(
                  {
                  "width": "100%%",
                  "height": "100%%",
                  "symbol": "BINANCE:BTCUSD",
                  "interval": "60",
                  "timezone": "Asia/Seoul",
                  "theme": "%s",
                  "style": "1",
                  "locale": "kr",
                  "enable_publishing": false,
                  "allow_symbol_change": true,
                  "container_id": "tradingview_widget",
                  "hide_side_toolbar": false,
                  "save_image": false
                }
                  );
                  </script>
                </div>
            </body>
            </html>
            """, backgroundColor, theme);
            
        chartWebView.getEngine().loadContent(content);
    }

    private void updateVBoxTheme(VBox vbox) {
        if (vbox.getStyle().contains("background-color")) {
            vbox.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 12;
                -fx-padding: 20;
                """, themeManager.getBgSecondary()));
        }
    }

    private void updateHBoxTheme(HBox hbox) {
        hbox.getChildren().forEach(node -> {
            if (node instanceof VBox card) {
                updateIndicatorCard(card);
            }
        });
    }

    private void updateIndicatorCard(VBox card) {
        // Find the accent color from the current style
        String currentStyle = card.getStyle();
        String accentColor = ThemeManager.COLOR_PRIMARY;
        if (currentStyle.contains("#f38ba8")) {
            accentColor = ThemeManager.COLOR_ERROR;
        } else if (currentStyle.contains("#89b4fa")) {
            accentColor = ThemeManager.COLOR_PRIMARY;
        } else if (currentStyle.contains("#a6e3a1")) {
            accentColor = ThemeManager.COLOR_SUCCESS;
        }
        
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-border-radius: 12;
            """, themeManager.getBgSecondary(), accentColor));
        
        // Update labels
        card.getChildren().forEach(node -> {
            if (node instanceof Label label) {
                if (label.getStyle().contains("font-size: 12px")) {
                    label.setStyle(String.format("""
                        -fx-font-size: 12px;
                        -fx-text-fill: %s;
                        -fx-font-family: 'Segoe UI';
                        """, themeManager.getTextSecondary()));
                }
            }
        });
    }

    private HBox createIndicatorsContainer() {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER);

        // RSI Indicator
        VBox rsiCard = createIndicatorCard("RSI (14)", "0.00", "#f38ba8");
        
        // MACD Indicator
        VBox macdCard = createIndicatorCard("MACD", "0.00", "#89b4fa");
        
        // Bollinger Bands Indicator
        VBox bbCard = createIndicatorCard("BB %B", "0.00", "#a6e3a1");

        HBox.setHgrow(rsiCard, Priority.ALWAYS);
        HBox.setHgrow(macdCard, Priority.ALWAYS);
        HBox.setHgrow(bbCard, Priority.ALWAYS);

        container.getChildren().addAll(rsiCard, macdCard, bbCard);
        
        return container;
    }

    private VBox createIndicatorCard(String name, String value, String accentColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 12;
            -fx-border-color: %s;
            -fx-border-width: 2;
            -fx-border-radius: 12;
            """, themeManager.getBgSecondary(), accentColor));

        Label nameLabel = new Label(name);
        nameLabel.setStyle(String.format("""
            -fx-font-size: 12px;
            -fx-text-fill: %s;
            -fx-font-family: 'Segoe UI';
            """, themeManager.getTextSecondary()));

        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("""
            -fx-font-size: 24px;
            -fx-text-fill: %s;
            -fx-font-weight: bold;
            -fx-font-family: 'Segoe UI';
            """, accentColor));

        card.getChildren().addAll(nameLabel, valueLabel);
        
        return card;
    }

    /**
     * Update the price display
     */
    public void updatePrice(double price, double changePercent) {
        priceLabel.setText(String.format("$%,.2f", price));
        
        // Update price label style to match current theme
        priceLabel.setStyle(String.format("""
            -fx-font-size: 36px;
            -fx-text-fill: %s;
            -fx-font-weight: bold;
            -fx-font-family: 'Segoe UI';
            """, themeManager.getTextPrimary()));
        
        // Use brighter, more visible colors for the change label
        String textColor;
        String bgColor;
        String sign = changePercent >= 0 ? "+" : "";
        
        if (changePercent >= 0) {
            // Positive change - Green
            textColor = "white";  // White text on green background
            bgColor = ThemeManager.COLOR_SUCCESS;  // Bright green background
        } else {
            // Negative change - Red
            textColor = "white";  // White text on red background
            bgColor = ThemeManager.COLOR_ERROR;  // Bright red background
        }
        
        changeLabel.setText(String.format("%s%.2f%%", sign, changePercent));
        changeLabel.setStyle(String.format("""
            -fx-font-size: 18px;
            -fx-text-fill: %s;
            -fx-font-weight: 600;
            -fx-padding: 5 12;
            -fx-background-color: %s;
            -fx-background-radius: 6;
            -fx-font-family: 'Segoe UI';
            """, textColor, bgColor));
    }

    /**
     * Update chart data - Deprecated for WebView but kept for compatibility
     */
    public void updateChart(List<Candle> candles) {
        // WebView handles chart updates internally
    }

    public void setQuestionnaireActionListener(QuestionnaireActionListener listener) {
        this.questionnaireListener = listener;
    }
    
    public interface QuestionnaireActionListener {
        void onQuestionnaireRequested();
    }
}
