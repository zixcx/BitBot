package com.bitbot.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * 거래 내역 화면 컨트롤러
 */
public class TradingHistoryController {

    @FXML
    private TableView<TradeRecord> tradingHistoryTable;
    
    @FXML
    private TableColumn<TradeRecord, String> dateColumn;
    
    @FXML
    private TableColumn<TradeRecord, String> symbolColumn;
    
    @FXML
    private TableColumn<TradeRecord, String> typeColumn;
    
    @FXML
    private TableColumn<TradeRecord, String> priceColumn;
    
    @FXML
    private TableColumn<TradeRecord, String> amountColumn;
    
    @FXML
    private TableColumn<TradeRecord, String> profitLossColumn;
    
    @FXML
    private Button backButton;

    /**
     * FXML 로드 시 자동으로 호출되는 초기화 메서드
     */
    @FXML
    public void initialize() {
        setupTableView();
        loadTradingHistory();
    }

    /**
     * 테이블 뷰 설정
     */
    private void setupTableView() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        profitLossColumn.setCellValueFactory(new PropertyValueFactory<>("profitLoss"));
    }

    /**
     * 거래 내역 데이터 로드
     */
    private void loadTradingHistory() {
        // TODO: 실제 데이터베이스에서 거래 내역 로드
        // 현재는 더미 데이터 표시
        tradingHistoryTable.getItems().add(new TradeRecord("2025-01-15 10:30", "BTC/USDT", "매수", "$42,500", "0.01", "+$125.00"));
        tradingHistoryTable.getItems().add(new TradeRecord("2025-01-15 11:45", "ETH/USDT", "매수", "$2,800", "0.5", "+$150.00"));
        tradingHistoryTable.getItems().add(new TradeRecord("2025-01-14 14:20", "BTC/USDT", "매도", "$42,300", "0.01", "+$75.00"));
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

    /**
     * 거래 내역 데이터 모델 클래스
     */
    public static class TradeRecord {
        private final String date;
        private final String symbol;
        private final String type;
        private final String price;
        private final String amount;
        private final String profitLoss;

        public TradeRecord(String date, String symbol, String type, String price, String amount, String profitLoss) {
            this.date = date;
            this.symbol = symbol;
            this.type = type;
            this.price = price;
            this.amount = amount;
            this.profitLoss = profitLoss;
        }

        public String getDate() { return date; }
        public String getSymbol() { return symbol; }
        public String getType() { return type; }
        public String getPrice() { return price; }
        public String getAmount() { return amount; }
        public String getProfitLoss() { return profitLoss; }
    }
}

