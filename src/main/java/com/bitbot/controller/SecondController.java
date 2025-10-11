package com.bitbot.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 두 번째 화면 컨트롤러
 */
public class SecondController {

    /**
     * FXML 로드 시 자동으로 호출되는 초기화 메서드
     */
    @FXML
    public void initialize() {
        System.out.println("SecondController initialized");
    }

    /**
     * 뒤로가기 버튼 클릭 이벤트 핸들러
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        // 현재 Scene에서 Stage와 크기 가져오기
        Scene currentScene = ((Node) event.getSource()).getScene();
        Stage window = (Stage) currentScene.getWindow();

        // 이전 화면(main.fxml) 로드
        Parent mainView = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        // 현재 창 크기와 동일한 Scene 생성
        Scene mainScene = new Scene(mainView, currentScene.getWidth(), currentScene.getHeight());

        // 이전 화면의 CSS 적용
        String css = getClass().getResource("/css/styles.css").toExternalForm();
        mainScene.getStylesheets().add(css);

        window.setScene(mainScene);
        window.show();
    }
}
