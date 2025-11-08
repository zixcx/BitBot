package com.bitbot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.bitbot.service.SupabaseService;

public class MainController {

    @FXML private TextField usernameField;   // 회원가입 시 사용
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    // ✅ 로그인 처리
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        String result = SupabaseService.signIn(email, password);

        if (result.contains("✅ 로그인 성공")) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("로그인 성공!");

            try {
                // 로그인 성공 후 화면 전환
                Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/second.fxml")), 800, 600);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("⚠️ 화면 전환 오류: " + e.getMessage());
            }
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText(result);
        }
    }
@FXML
private void goToSignUp(ActionEvent event) {
    try {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/signup.fxml")), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    // ✅ 회원가입 처리
    @FXML
    private void handleSignUp(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        String result = SupabaseService.signUp(username, email, password);

        if (result.contains("✅")) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("회원가입 성공! 로그인해주세요.");
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText(result);
        }
    }
}
