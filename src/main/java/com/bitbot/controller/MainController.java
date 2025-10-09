package com.bitbot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

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
    private void handleStartButton() {
        System.out.println("Start button clicked");
        titleLabel.setText("BitBot Started!");
    }
}

