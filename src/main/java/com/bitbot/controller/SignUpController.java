package com.bitbot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.bitbot.service.SupabaseService;

public class SignUpController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    // ✅ 회원가입 버튼 클릭 시
    @FXML
    private void handleSignUp(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        String result = SupabaseService.signUp(username, email, password);

        if (result.contains("✅")) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("회원가입 성공! 로그인 페이지로 이동합니다.");
            goToLogin(event);
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText(result);
        }
    }

    // ✅ 로그인 페이지로 돌아가기
    @FXML
    private void goToLogin(ActionEvent event) {
        try {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

        // ✅ main.fxml 로드
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);

        // ✅ 스타일시트 다시 적용 (중요)
        String css = getClass().getResource("/css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setResizable(true);
        stage.show();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

}