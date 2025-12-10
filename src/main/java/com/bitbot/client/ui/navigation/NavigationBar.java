package com.bitbot.client.ui.navigation;

import com.bitbot.client.service.ThemeManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Navigation Bar Component
 * Left sidebar with icon-based menu navigation
 * 
 * Menu Items:
 * - Dashboard: Main overview screen
 * - Portfolio: Asset status and P&L
 * - Journal: Trade history
 * - Settings: API keys and preferences (bottom)
 * - Theme Toggle: Light/Dark mode (bottom)
 */
public class NavigationBar extends VBox {
    
    private static final double NAV_WIDTH = 80;
    private static final double ICON_SIZE = 24;
    
    private final ThemeManager themeManager;
    private NavigationListener listener;
    private Button activeButton;
    private Button themeToggleButton;

    public NavigationBar() {
        this.themeManager = ThemeManager.getInstance();
        initializeUI();
        setupThemeBinding();
    }

    private void initializeUI() {
        setAlignment(Pos.TOP_CENTER);
        setPrefWidth(NAV_WIDTH);
        setMinWidth(NAV_WIDTH);
        setMaxWidth(NAV_WIDTH);
        setSpacing(10);
        setPadding(new javafx.geometry.Insets(20, 0, 20, 0));
        
        applyTheme();

        // Main navigation buttons
        Button dashboardBtn = createNavButton(FontAwesomeSolid.HOME, "Dashboard", NavigationItem.DASHBOARD);
        Button portfolioBtn = createNavButton(FontAwesomeSolid.BRIEFCASE, "Portfolio", NavigationItem.PORTFOLIO);
        Button journalBtn = createNavButton(FontAwesomeSolid.BOOK, "Journal", NavigationItem.JOURNAL);
        Button questionnaireBtn = createNavButton(FontAwesomeSolid.CLIPBOARD_LIST, "설문조사", NavigationItem.QUESTIONNAIRE);

        // Spacer to push bottom buttons down
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Bottom section
        Separator separator = new Separator();
        separator.setMaxWidth(60);
        
        // Settings button
        Button settingsBtn = createNavButton(FontAwesomeSolid.COG, "Settings", NavigationItem.SETTINGS);
        
        // Theme toggle button
        themeToggleButton = createThemeToggleButton();

        getChildren().addAll(
            dashboardBtn, 
            portfolioBtn, 
            journalBtn,
            questionnaireBtn,
            spacer,
            separator,
            settingsBtn,
            themeToggleButton
        );
        
        // Set dashboard as active by default
        setActiveButton(dashboardBtn);
    }

    private Button createNavButton(FontAwesomeSolid icon, String tooltipText, NavigationItem navItem) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize((int) ICON_SIZE);
        updateIconColor(fontIcon);
        
        Button button = new Button();
        button.setGraphic(fontIcon);
        button.setPrefSize(60, 60);
        button.setTooltip(new Tooltip(tooltipText));
        button.getStyleClass().add("nav-button");
        button.setUserData(navItem); // Store the NavigationItem for later reference
        
        applyButtonStyle(button, false);
        
        // Hover effect
        button.setOnMouseEntered(e -> {
            if (button != activeButton) {
                applyButtonStyle(button, true);
            }
        });
        
        button.setOnMouseExited(e -> {
            if (button != activeButton) {
                applyButtonStyle(button, false);
            }
        });
        
        // Click action
        button.setOnAction(e -> {
            setActiveButton(button);
            if (listener != null) {
                listener.onNavigationItemSelected(navItem);
            }
        });
        
        return button;
    }

    private Button createThemeToggleButton() {
        FontIcon icon = new FontIcon(
            themeManager.isDarkMode() ? FontAwesomeSolid.SUN : FontAwesomeSolid.MOON
        );
        icon.setIconSize((int) ICON_SIZE);
        updateIconColor(icon);
        
        Button button = new Button();
        button.setGraphic(icon);
        button.setPrefSize(60, 60);
        button.setTooltip(new Tooltip("Toggle Theme"));
        
        applyButtonStyle(button, false);
        
        button.setOnMouseEntered(e -> applyButtonStyle(button, true));
        button.setOnMouseExited(e -> applyButtonStyle(button, false));
        
        button.setOnAction(e -> {
            themeManager.toggleTheme();
            // Update icon
            FontIcon newIcon = new FontIcon(
                themeManager.isDarkMode() ? FontAwesomeSolid.SUN : FontAwesomeSolid.MOON
            );
            newIcon.setIconSize((int) ICON_SIZE);
            updateIconColor(newIcon);
            button.setGraphic(newIcon);
        });
        
        return button;
    }

    private void applyButtonStyle(Button button, boolean isHover) {
        String bgColor = isHover ? themeManager.getBgTertiary() : "transparent";
        String textColor = isHover ? ThemeManager.COLOR_PRIMARY : themeManager.getTextPrimary();
        
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-background-radius: 12;
            -fx-cursor: hand;
            """, bgColor, textColor));
        
        // Update icon color
        if (button.getGraphic() instanceof FontIcon fontIcon) {
            fontIcon.setIconColor(javafx.scene.paint.Color.web(textColor));
        }
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            applyButtonStyle(activeButton, false);
        }
        
        activeButton = button;
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-background-radius: 12;
            -fx-cursor: hand;
            """, ThemeManager.COLOR_PRIMARY, "#ffffff"));
        
        // Update icon color to white for active button
        if (button.getGraphic() instanceof FontIcon fontIcon) {
            fontIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        }
    }

    private void updateIconColor(FontIcon icon) {
        icon.setIconColor(javafx.scene.paint.Color.web(themeManager.getTextPrimary()));
    }

    private void applyTheme() {
        setStyle(String.format("""
            -fx-background-color: %s;
            -fx-padding: 20 0;
            """, themeManager.getBgSecondary()));
    }

    private void setupThemeBinding() {
        themeManager.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            applyTheme();
            // Update all button styles
            getChildren().forEach(node -> {
                if (node instanceof Button btn && btn != activeButton) {
                    applyButtonStyle(btn, false);
                }
            });
            // Update active button
            if (activeButton != null) {
                setActiveButton(activeButton);
            }
        });
    }

    public void setNavigationListener(NavigationListener listener) {
        this.listener = listener;
    }

    /**
     * Programmatically select a navigation item
     */
    public void selectItem(NavigationItem targetItem) {
        // Find the button for this specific item and trigger its action
        getChildren().stream()
            .filter(node -> node instanceof Button)
            .map(node -> (Button) node)
            .filter(button -> button.getUserData() == targetItem)
            .findFirst()
            .ifPresent(Button::fire);
    }

    public enum NavigationItem {
        DASHBOARD,
        PORTFOLIO,
        JOURNAL,
        QUESTIONNAIRE,
        SETTINGS
    }

    @FunctionalInterface
    public interface NavigationListener {
        void onNavigationItemSelected(NavigationItem item);
    }
}


