package com.bitbot.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.event.ActionEvent;

/**
 * 메인 화면 컨트롤러
 */
public class MainController {

    @FXML
    private Label titleLabel;

    @FXML
    private Button startButton;

    /**
     * FXML 로드 시 자동으로 호출되는 초기화 메서드
     */
    @FXML
    public void initialize() {
        // 초기화 로직
        System.out.println("MainController initialized");
    }

    /**
     * 시작 버튼 클릭 이벤트 핸들러
     */
    @FXML
    private void handleStartButton(ActionEvent event) throws IOException {
        System.out.println("Start button clicked");

        // 현재 Scene에서 Stage와 크기 가져오기
        Scene currentScene = ((Node) event.getSource()).getScene();
        Stage window = (Stage) currentScene.getWindow();

        // 대시보드 화면 로드
        Parent dashboardView = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
        // 현재 창 크기와 동일한 Scene 생성
        Scene dashboardScene = new Scene(dashboardView, currentScene.getWidth(), currentScene.getHeight());

        // 대시보드 화면의 CSS 적용
        String css = getClass().getResource("/css/styles.css").toExternalForm();
        dashboardScene.getStylesheets().add(css);

        window.setScene(dashboardScene);
        window.show();
    }
}

