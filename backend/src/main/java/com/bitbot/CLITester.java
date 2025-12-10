package com.bitbot;

import com.bitbot.database.UserProfileRepository;
import com.bitbot.models.UserProfile;
import com.bitbot.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * CLI 테스트 프로그램
 * GUI 없이 거래 엔진을 테스트
 */
public class CLITester {
    
    private static final Logger logger = LoggerFactory.getLogger(CLITester.class);
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("BitBot - LLM 기반 자동 거래 시스템 CLI 테스터");
        logger.info("=".repeat(80));
        
        try {
            // 환경 설정 로드
            ConfigLoader.loadConfig();
            logger.info("✅ 환경 설정 로드 완료");
            logger.info("거래 모드: {}", ConfigLoader.getTradingMode());
            logger.info("Testnet: {}", ConfigLoader.isTestnet());
            
            // 사용자 ID 설정 (기본값: 1)
            Integer userId = 1;
            
            // 사용자 프로필 확인
            UserProfileRepository profileRepo = new UserProfileRepository();
            UserProfile profile = profileRepo.findByUserId(userId);
            
            if (profile == null) {
                System.out.println("\n⚠️ 사용자 프로필이 없습니다.");
                System.out.print("설문조사를 진행하시겠습니까? (y/n): ");
                String answer = scanner.nextLine().trim().toLowerCase();
                
                if (answer.equals("y") || answer.equals("yes")) {
                    CLIQuestionnaire questionnaire = new CLIQuestionnaire();
                    questionnaire.runQuestionnaire(userId);
                    profile = profileRepo.findByUserId(userId);
                } else {
                    System.out.println("기본 설정으로 진행합니다.\n");
                }
            } else {
                System.out.println("\n✅ 사용자 프로필 발견:");
                System.out.println(profile);
                System.out.println();
            }
            
            // 거래 엔진 초기화
            TradingEngine engine = new TradingEngine();
            engine.setUserId(userId);
            
            // 연결 테스트
            logger.info("\n" + "=".repeat(80));
            logger.info("시스템 연결 테스트");
            logger.info("=".repeat(80));
            
            if (!engine.testConnections()) {
                logger.error("❌ 시스템 연결 테스트 실패. 프로그램을 종료합니다.");
                System.exit(1);
            }
            
            logger.info("✅ 모든 연결 테스트 통과\n");
            
            // 1회 거래 사이클 실행
            logger.info("거래 사이클 실행을 시작합니다...\n");
            engine.runOneCycle();
            
            logger.info("\n✅ 테스트 완료!");
            
        } catch (Exception e) {
            logger.error("프로그램 실행 중 오류 발생", e);
            System.exit(1);
        }
    }
}


