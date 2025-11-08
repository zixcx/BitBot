package com.bitbot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.bitbot.service.AuthStorage;

public class SecondController {

    @FXML private Label userInfoLabel;

    @FXML
    public void initialize() {
        String email = AuthStorage.getEmail();
        userInfoLabel.setText("현재 로그인: " + email);
    }

    // ✅ 로그아웃 버튼 (onAction="#handleLogout")
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // JWT, refresh_token 제거
            AuthStorage.clear();

            // main.fxml로 전환 (로그인 화면)
            Stage stage = (Stage) userInfoLabel.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/main.fxml")), 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
