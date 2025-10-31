package com.bitbot.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * 설정 화면 컨트롤러
 */
public class SettingsController {

    @FXML
    private TextField apiKeyField;
    
    @FXML
    private PasswordField secretKeyField;
    
    @FXML
    private TextField maxInvestmentPercentField;
    
    @FXML
    private TextField stopLossPercentField;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label statusLabel;

    /**
     * FXML 로드 시 자동으로 호출되는 초기화 메서드
     */
    @FXML
    public void initialize() {
        loadSettings();
    }

    /**
     * 설정 데이터 로드
     */
    private void loadSettings() {
        // TODO: 실제 설정 파일이나 환경 변수에서 로드
        // 현재는 더미 데이터 표시
        maxInvestmentPercentField.setText("10");
        stopLossPercentField.setText("-10");
    }

    /**
     * 저장 버튼 클릭 이벤트 핸들러
     */
    @FXML
    private void handleSaveButton(ActionEvent event) {
        // TODO: 실제 설정 저장 로직 구현
        statusLabel.setText("설정이 저장되었습니다.");
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    /**
     * 뒤로가기 버튼 클릭 이벤트 핸들러
     */
    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        navigateToScene(event, "/fxml/dashboard.fxml");
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

