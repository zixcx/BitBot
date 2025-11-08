package com.bitbot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.bitbot.service.AuthStorage;
import com.bitbot.service.SupabaseService;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root;

        // ✅ 저장된 로그인 세션 확인
        String savedToken = AuthStorage.getAccessToken();
        String savedEmail = AuthStorage.getEmail();

        if (savedToken != null && savedEmail != null) {
            // ✅ refresh_token으로 세션 갱신 시도
            boolean refreshed = SupabaseService.refreshSession();
            if (refreshed) {
                root = FXMLLoader.load(getClass().getResource("/fxml/second.fxml"));
            } else {
                AuthStorage.clear();
                root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
            }
        } else {
            root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        }

        Scene scene = new Scene(root, 800, 600);
        String css = getClass().getResource("/css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("BitBot");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "true");
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.allowhidpi", "true");
        launch(args);
    }
}