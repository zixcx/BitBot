package com.bitbot.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * 대시보드 화면 컨트롤러
 */
public class DashboardController {

    @FXML
    private Label totalBalanceLabel;
    
    @FXML
    private Label profitLossLabel;
    
    @FXML
    private Label profitLossPercentLabel;
    
    @FXML
    private Label activeTradesLabel;
    
    @FXML
    private Label totalTradesLabel;
    
    @FXML
    private Label winRateLabel;

    @FXML
    private Button tradingHistoryButton;
    
    @FXML
    private Button settingsButton;

    /**
     * FXML 로드 시 자동으로 호출되는 초기화 메서드
     */
    @FXML
    public void initialize() {
        // 초기 데이터 로드
        loadDashboardData();
    }

    /**
     * 대시보드 데이터 로드
     */
    private void loadDashboardData() {
        // TODO: 실제 데이터베이스나 API에서 데이터 로드
        // 현재는 더미 데이터 표시
        totalBalanceLabel.setText("$10,000.00");
        profitLossLabel.setText("+$500.00");
        profitLossPercentLabel.setText("+5.0%");
        activeTradesLabel.setText("3");
        totalTradesLabel.setText("25");
        winRateLabel.setText("68%");
    }

    /**
     * 거래 내역 화면으로 이동
     */
    @FXML
    private void handleTradingHistoryButton(ActionEvent event) throws IOException {
        navigateToScene(event, "/fxml/trading-history.fxml");
    }

    /**
     * 설정 화면으로 이동
     */
    @FXML
    private void handleSettingsButton(ActionEvent event) throws IOException {
        navigateToScene(event, "/fxml/settings.fxml");
    }

    /**
     * 메인 화면으로 돌아가기
     */
    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        navigateToScene(event, "/fxml/main.fxml");
    }

    /**
     * 화면 전환 헬퍼 메서드
     */
    private void navigateToScene(ActionEvent event, String fxmlPath) throws IOException {
        Scene currentScene = ((Node) event.getSource()).getScene();
        Stage window = (Stage) currentScene.getWindow();
        
        Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(view, currentScene.getWidth(), currentScene.getHeight());
        
        String css = getClass().getResource("/css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        
        window.setScene(scene);
        window.show();
    }
}

