package com.bitbot.server;

import com.bitbot.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot 서버 애플리케이션
 * REST API를 제공하는 서버
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.bitbot")
@ServletComponentScan
public class TradingBotServerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(TradingBotServerApplication.class);
    
    public static void main(String[] args) {
        // 환경 변수 로드
        ConfigLoader.loadConfig();
        
        logger.info("=".repeat(80));
        logger.info("BitBot Trading Server 시작 중...");
        logger.info("=".repeat(80));
        
        SpringApplication.run(TradingBotServerApplication.class, args);
        
        logger.info("✅ BitBot Trading Server 시작 완료");
        logger.info("REST API: http://localhost:8080/api");
    }
}

