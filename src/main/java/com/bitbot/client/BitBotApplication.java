package com.bitbot.client;

import com.bitbot.client.model.Candle;
import com.bitbot.client.service.MarketDataService;
import com.bitbot.client.service.ThemeManager;
import com.bitbot.client.service.api.ServerApiClient;
import com.bitbot.client.ui.auth.LoginView;
import com.bitbot.client.ui.auth.RegisterView;
import com.bitbot.client.ui.dashboard.DashboardView;
import com.bitbot.client.ui.feed.AgentFeedView;
import com.bitbot.client.ui.navigation.NavigationBar;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * BitBot JavaFX Application
 * Bitcoin Investment Trading Bot with AI-Driven Decision Making
 * 
 * @version 1.0.0
 * @author BitBot Team
 */
public class BitBotApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(BitBotApplication.class);
    private static final String APP_TITLE = "BitBot - AI Trading Assistant";
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final int MARKET_DATA_UPDATE_INTERVAL = 60; // seconds
    
    private Stage primaryStage;
    private Scene mainScene;
    private ThemeManager themeManager;
    private ServerApiClient serverApiClient;
    
    // Main container
    private StackPane rootContainer;
    
    // Auth views
    private LoginView loginView;
    private RegisterView registerView;
    
    // Main app views
    private BorderPane mainAppLayout;
    private NavigationBar navigationBar;
    private DashboardView dashboardView;
    private AgentFeedView agentFeedView;
    private com.bitbot.client.ui.portfolio.PortfolioView portfolioView;
    private com.bitbot.client.ui.journal.JournalView journalView;
    private com.bitbot.client.ui.settings.SettingsView settingsView;
    private MarketDataService marketDataService;
    
    // Theme toggle button
    private Button themeToggleButton;
    
    private boolean isAuthenticated = false;

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            logger.info("Starting BitBot Application...");

            // Initialize theme manager
            themeManager = ThemeManager.getInstance();
            
            // Initialize server API client
            serverApiClient = new ServerApiClient();
            
            // Create root container
            rootContainer = new StackPane();
            
            // Show login screen first
            showLoginScreen();
            
            // Create scene
            mainScene = new Scene(rootContainer, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            // Configure stage
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(mainScene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            
            // Handle window close
            primaryStage.setOnCloseRequest(e -> stop());
            
            // Show stage
            primaryStage.show();
            
            logger.info("BitBot Application started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start BitBot Application", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Show login screen
     */
    private void showLoginScreen() {
        loginView = new LoginView(serverApiClient);
        loginView.setListener(new LoginView.LoginListener() {
            @Override
            public void onLoginSuccess(String email, String sessionToken) {
                logger.info("User logged in: {}", email);
                isAuthenticated = true;
                showMainApplication();
            }

            @Override
            public void onRegisterRequest() {
                showRegisterScreen();
            }
        });
        
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(loginView);
        
        // Add theme toggle button
        addThemeToggleButton();
    }

    /**
     * Show registration screen
     */
    private void showRegisterScreen() {
        registerView = new RegisterView(serverApiClient);
        registerView.setListener(new RegisterView.RegisterListener() {
            @Override
            public void onRegisterSuccess(String email) {
                logger.info("User registered: {}", email);
                showLoginScreen();
            }

            @Override
            public void onBackToLogin() {
                showLoginScreen();
            }
        });
        
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(registerView);
        
        // Add theme toggle button
        addThemeToggleButton();
    }

    /**
     * Show main application (after login)
     */
    private void showMainApplication() {
        // Initialize services
        initializeServices();
        
        // Create main layout
        mainAppLayout = createMainLayout();
        
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(mainAppLayout);
        
        // Add theme toggle button
        addThemeToggleButton();
        
        // Start market data updates
        marketDataService.start(MARKET_DATA_UPDATE_INTERVAL);
        
        logger.info("Main application loaded");
    }

    /**
     * Initialize application services
     */
    private void initializeServices() {
        // Initialize dashboard and agent feed first
        dashboardView = new DashboardView(serverApiClient);
        dashboardView.setQuestionnaireActionListener(() -> {
            navigationBar.selectItem(NavigationBar.NavigationItem.QUESTIONNAIRE);
        });
        agentFeedView = new AgentFeedView(serverApiClient);
        
        // Market data service
        marketDataService = new MarketDataService();
        marketDataService.setListener(new MarketDataService.MarketDataListener() {
            @Override
            public void onPriceUpdate(double price, double changePercent) {
                if (dashboardView != null) {
                    dashboardView.updatePrice(price, changePercent);
                }
                logger.debug("Price updated: ${} ({}%)", price, changePercent);
            }

            @Override
            public void onCandlesUpdate(List<Candle> candles) {
                if (dashboardView != null) {
                    dashboardView.updateChart(candles);
                }
                logger.debug("Chart updated with {} candles", candles.size());
            }
        });
        
        // Check connectivity
        marketDataService.checkConnectivity().thenAccept(connected -> {
            if (connected) {
                logger.info("✓ Connected to Binance API");
                // Connected - agent feed will show monitoring status by default
            } else {
                logger.error("✗ Failed to connect to Binance API");
                // Connection failed - but feed will still work
            }
        });
    }

    /**
     * Creates the main BorderPane layout
     * Layout structure:
     * - LEFT: Navigation Bar (60-80px fixed)
     * - CENTER: Dashboard (Main content area)
     * - RIGHT: Agent Feed (Activity log)
     */
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        
        // Apply theme
        updateMainLayoutTheme(root);
        
        // Listen to theme changes
        themeManager.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            updateMainLayoutTheme(root);
        });

        // Navigation Bar (LEFT)
        navigationBar = new NavigationBar();
        navigationBar.setNavigationListener(item -> {
            logger.info("Navigation item selected: {}", item);
            // TODO: Switch views based on navigation item
            switch (item) {
                case DASHBOARD:
                    if (dashboardView == null) {
                        dashboardView = new com.bitbot.client.ui.dashboard.DashboardView(serverApiClient);
                        dashboardView.setQuestionnaireActionListener(() -> {
                            // Navigate to questionnaire
                            navigationBar.selectItem(com.bitbot.client.ui.navigation.NavigationBar.NavigationItem.QUESTIONNAIRE);
                        });
                    }
                    root.setCenter(dashboardView);
                    root.setRight(agentFeedView); // Show agent feed only on dashboard
                    break;
                case PORTFOLIO:
                    if (portfolioView == null) {
                        portfolioView = new com.bitbot.client.ui.portfolio.PortfolioView();
                    }
                    root.setCenter(portfolioView);
                    root.setRight(null); // Hide agent feed
                    break;
                case JOURNAL:
                    if (journalView == null) {
                        journalView = new com.bitbot.client.ui.journal.JournalView();
                    }
                    root.setCenter(journalView);
                    root.setRight(null); // Hide agent feed
                    break;
                case QUESTIONNAIRE:
                    com.bitbot.client.ui.questionnaire.QuestionnaireView questionnaireView = 
                        new com.bitbot.client.ui.questionnaire.QuestionnaireView(serverApiClient);
                    questionnaireView.setListener(result -> {
                        javafx.application.Platform.runLater(() -> {
                            // Show success and return to dashboard
                            logger.info("Questionnaire completed successfully");
                            dashboardView = null; // Reset to refresh banner
                            navigationBar.selectItem(com.bitbot.client.ui.navigation.NavigationBar.NavigationItem.DASHBOARD);
                        });
                    });
                    root.setCenter(questionnaireView);
                    root.setRight(null); // Hide agent feed
                    break;
                case SETTINGS:
                    if (settingsView == null) {
                        settingsView = new com.bitbot.client.ui.settings.SettingsView(serverApiClient);
                    }
                    root.setCenter(settingsView);
                    root.setRight(null); // Hide agent feed
                    break;
            }
        });
        root.setLeft(navigationBar);

        // Dashboard (CENTER)
        root.setCenter(dashboardView);

        // Agent Feed (RIGHT)
        root.setRight(agentFeedView);

        return root;
    }

    /**
     * Update main layout theme
     */
    private void updateMainLayoutTheme(BorderPane root) {
        root.setStyle(String.format("-fx-background-color: %s;", themeManager.getBgPrimary()));
    }

    /**
     * Add theme toggle button (removed - now in navigation bar)
     */
    private void addThemeToggleButton() {
        // Theme toggle is now in the navigation bar
        // No floating button needed
    }

    @Override
    public void stop() {
        logger.info("Shutting down BitBot Application...");
        if (marketDataService != null) {
            marketDataService.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
