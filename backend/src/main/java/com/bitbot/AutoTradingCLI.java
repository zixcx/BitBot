package com.bitbot;

import com.bitbot.database.UserProfileRepository;
import com.bitbot.models.UserProfile;
import com.bitbot.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * 자동 거래 CLI 프로그램
 * 주기적으로 거래 사이클을 자동 실행
 */
public class AutoTradingCLI {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoTradingCLI.class);
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("🤖 BitBot - 자동 거래 시스템");
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
            
            // 전략 기반 실행 주기 결정
            com.bitbot.models.TradingStrategy strategy = null;
            int intervalMinutes;
            
            if (profile != null) {
                strategy = profile.getTradingStrategy();
                intervalMinutes = getIntervalForStrategy(strategy);
                logger.info("사용자 프로필 기반 실행 주기 설정: {} 전략 → {}분마다 실행", 
                        strategy.getKoreanName(), intervalMinutes);
            } else {
                // 프로필이 없으면 환경 변수 또는 기본값 사용
                intervalMinutes = ConfigLoader.getInt("ANALYSIS_INTERVAL_MINUTES", 15);
                logger.info("프로필 없음 - 환경 변수 기반 실행 주기: {}분마다 실행", intervalMinutes);
            }
            
            // 자동 거래 서비스 생성 및 시작 (전략 전달)
            AutoTradingService autoTrading = new AutoTradingService(engine, strategy);
            autoTrading.registerShutdownHook();
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("자동 거래 시스템 시작");
            System.out.println("=".repeat(80));
            if (strategy != null) {
                System.out.println("거래 전략: " + strategy.getKoreanName());
                System.out.println("전략 시간봉: " + getTimeframeForStrategy(strategy));
            }
            System.out.println("실행 간격: " + intervalMinutes + "분 (" + 
                    String.format("%.1f", intervalMinutes / 60.0) + "시간)");
            System.out.println("거래 모드: " + ConfigLoader.getTradingMode());
            System.out.println("\n종료하려면 'q' 또는 'quit'를 입력하세요.");
            System.out.println("=".repeat(80) + "\n");
            
            autoTrading.start();
            
            // 사용자 입력 대기 (종료 명령 처리)
            while (autoTrading.isRunning()) {
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("q") || input.equals("quit") || input.equals("exit")) {
                    System.out.println("\n종료 요청 수신...");
                    autoTrading.stop();
                    break;
                } else if (input.equals("status")) {
                    System.out.println("자동 거래 상태: " + (autoTrading.isRunning() ? "실행 중" : "중지됨"));
                } else if (!input.isEmpty()) {
                    System.out.println("알 수 없는 명령입니다. 종료하려면 'q'를 입력하세요.");
                }
            }
            
            logger.info("\n✅ 프로그램 종료");
            
        } catch (Exception e) {
            logger.error("프로그램 실행 중 오류 발생", e);
            System.exit(1);
        } finally {
            scanner.close();
        }
    }
    
    /**
     * 전략별 실행 주기 반환 (분 단위)
     */
    private static int getIntervalForStrategy(com.bitbot.models.TradingStrategy strategy) {
        switch (strategy) {
            case SPOT_DCA:
                return 240; // 4시간
            case TREND_FOLLOWING:
                return 240; // 4시간
            case SWING_TRADING:
                return 60;  // 1시간
            case VOLATILITY_BREAKOUT:
                return 15;  // 15분
            default:
                return com.bitbot.utils.ConfigLoader.getInt("ANALYSIS_INTERVAL_MINUTES", 15);
        }
    }
    
    /**
     * 전략별 시간봉 반환
     */
    private static String getTimeframeForStrategy(com.bitbot.models.TradingStrategy strategy) {
        switch (strategy) {
            case SPOT_DCA:
                return "1d";
            case TREND_FOLLOWING:
                return "4h";
            case SWING_TRADING:
                return "1h";
            case VOLATILITY_BREAKOUT:
                return "15m";
            default:
                return "15m";
        }
    }
}

