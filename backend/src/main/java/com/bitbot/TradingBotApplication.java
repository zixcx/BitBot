package com.bitbot;

import com.bitbot.utils.ConfigLoader;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LLM 에이전트 기반 바이낸스 자동 거래 시스템
 * 메인 애플리케이션 클래스
 */
public class TradingBotApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(TradingBotApplication.class);
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("=".repeat(60));
            logger.info("LLM 에이전트 기반 자동 거래 시스템 시작");
            logger.info("=".repeat(60));
            
            // 환경 변수 로드
            ConfigLoader.loadConfig();
            logger.info("환경 설정 로드 완료");
            
            // TODO: GUI 초기화
            primaryStage.setTitle("BitBot - LLM Trading System");
            primaryStage.setWidth(1400);
            primaryStage.setHeight(900);
            
            logger.info("애플리케이션 초기화 완료");
            
            primaryStage.show();
            
        } catch (Exception e) {
            logger.error("애플리케이션 시작 실패", e);
            System.exit(1);
        }
    }
    
    @Override
    public void stop() {
        logger.info("애플리케이션 종료 중...");
        // TODO: 리소스 정리
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}


