package com.bitbot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX 메인 애플리케이션 클래스
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // FXML 파일 로드
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        // Scene 생성
        Scene scene = new Scene(root, 800, 600);

        // 전역 CSS 스타일시트 적용
        String css = getClass().getResource("/css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        // Stage 설정
        primaryStage.setTitle("BitBot");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    /**
     * 애플리케이션 진입점
     */
    public static void main(String[] args) {
        launch(args);
    }
}

