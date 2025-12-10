package com.bitbot;

import com.bitbot.database.TradeRepository;
import com.bitbot.data.BinanceDataCollector;
import com.bitbot.models.AccountInfo;
import com.bitbot.models.TradeOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * 통합 테스트 프로그램
 * 여러 거래 사이클을 실행하고 수익률을 추적
 */
public class IntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTest.class);
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        logger.info("=".repeat(80));
        logger.info("BitBot 통합 테스트 프로그램");
        logger.info("=".repeat(80));
        
        try {
            // 환경 설정 로드
            com.bitbot.utils.ConfigLoader.loadConfig();
            logger.info("✅ 환경 설정 로드 완료\n");
            
            // 사용자 프로필 확인
            com.bitbot.database.UserProfileRepository profileRepo = 
                    new com.bitbot.database.UserProfileRepository();
            com.bitbot.models.UserProfile profile = profileRepo.findByUserId(1);
            
            if (profile == null) {
                logger.warn("⚠️ 사용자 프로필이 없습니다. 설문조사를 먼저 진행하세요.");
                logger.info("설문조사 진행: .\\run-cli.bat");
                return;
            }
            
            logger.info("✅ 사용자 프로필 확인:");
            logger.info("  - 투자 성향: {}", profile.getInvestorType().getKoreanName());
            logger.info("  - 거래 전략: {}", profile.getTradingStrategy().getKoreanName());
            logger.info("  - 손절 기준: {}%", String.format("%.1f", 
                    profile.getRiskSettings().getStopLossPercent()));
            logger.info("  - 익절 기준: {}%", String.format("%.1f", 
                    profile.getRiskSettings().getTakeProfitPercent()));
            logger.info("");
            
            // 초기 계좌 정보
            BinanceDataCollector dataCollector = new BinanceDataCollector();
            AccountInfo initialAccount = dataCollector.getAccountInfo();
            
            logger.info("=".repeat(80));
            logger.info("초기 계좌 상태");
            logger.info("=".repeat(80));
            printAccountInfo(initialAccount);
            logger.info("");
            
            // 테스트 설정
            System.out.print("거래 사이클 실행 횟수 (기본: 5): ");
            String cycleInput = scanner.nextLine().trim();
            int cycles = cycleInput.isEmpty() ? 5 : Integer.parseInt(cycleInput);
            
            System.out.print("사이클 간 대기 시간(초) (기본: 10): ");
            String waitInput = scanner.nextLine().trim();
            int waitSeconds = waitInput.isEmpty() ? 10 : Integer.parseInt(waitInput);
            
            logger.info("");
            logger.info("=".repeat(80));
            logger.info("테스트 시작: {}회 거래 사이클 실행", cycles);
            logger.info("사이클 간 대기: {}초", waitSeconds);
            logger.info("=".repeat(80));
            logger.info("");
            
            // TradingEngine 초기화
            TradingEngine engine = new TradingEngine();
            engine.setUserId(1);
            
            // 거래 사이클 실행
            for (int i = 1; i <= cycles; i++) {
                logger.info("\n" + "=".repeat(80));
                logger.info("거래 사이클 {}/{}", i, cycles);
                logger.info("=".repeat(80));
                logger.info("시작 시간: {}", 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                try {
                    // 거래 사이클 실행
                    engine.runOneCycle();
                    
                    // 현재 계좌 정보 조회
                    AccountInfo currentAccount = dataCollector.getAccountInfo();
                    
                    logger.info("\n[사이클 {} 완료] 현재 상태:", i);
                    printAccountInfo(currentAccount);
                    
                    // 손익 계산
                    double profitLoss = currentAccount.getTotalBalance() - initialAccount.getTotalBalance();
                    double profitLossPercent = (profitLoss / initialAccount.getTotalBalance()) * 100.0;
                    
                    logger.info("초기 대비 손익: ${} ({})", 
                            String.format("%.2f", profitLoss),
                            String.format("%.2f%%", profitLossPercent));
                    
                    // 거래 내역 확인
                    TradeRepository tradeRepo = new TradeRepository();
                    List<TradeOrder> recentTrades = tradeRepo.findRecentTrades("1", 5);
                    logger.info("최근 거래: {}건", recentTrades.size());
                    
                    if (i < cycles) {
                        logger.info("\n{}초 후 다음 사이클 실행...", waitSeconds);
                        Thread.sleep(waitSeconds * 1000);
                    }
                    
                } catch (Exception e) {
                    logger.error("거래 사이클 {} 실행 중 오류 발생", i, e);
                }
            }
            
            // 최종 결과
            logger.info("\n" + "=".repeat(80));
            logger.info("테스트 완료 - 최종 결과");
            logger.info("=".repeat(80));
            
            AccountInfo finalAccount = dataCollector.getAccountInfo();
            
            logger.info("\n[초기 상태]");
            printAccountInfo(initialAccount);
            
            logger.info("\n[최종 상태]");
            printAccountInfo(finalAccount);
            
            // 최종 손익 계산
            double totalProfitLoss = finalAccount.getTotalBalance() - initialAccount.getTotalBalance();
            double totalProfitLossPercent = (totalProfitLoss / initialAccount.getTotalBalance()) * 100.0;
            
            logger.info("\n[최종 손익]");
            logger.info("  총 손익: ${}", String.format("%.2f", totalProfitLoss));
            logger.info("  손익률: {}%", String.format("%.2f", totalProfitLossPercent));
            
            // 거래 통계
            TradeRepository tradeRepo = new TradeRepository();
            List<TradeOrder> allTrades = tradeRepo.findRecentTrades("1", 1000);
            
            long buyCount = allTrades.stream()
                    .filter(t -> t.getType() == TradeOrder.OrderType.MARKET_BUY 
                            && t.getStatus() == TradeOrder.OrderStatus.FILLED)
                    .count();
            long sellCount = allTrades.stream()
                    .filter(t -> t.getType() == TradeOrder.OrderType.MARKET_SELL 
                            && t.getStatus() == TradeOrder.OrderStatus.FILLED)
                    .count();
            
            logger.info("\n[거래 통계]");
            logger.info("  총 거래 횟수: {}건", allTrades.size());
            logger.info("  매수 거래: {}건", buyCount);
            logger.info("  매도 거래: {}건", sellCount);
            
            // 손절/익절 발생 여부 확인
            if (finalAccount.getProfitLossPercent() <= profile.getRiskSettings().getStopLossPercent()) {
                logger.warn("\n⚠️ 손절 기준 도달! ({}% <= {}%)", 
                        String.format("%.2f", finalAccount.getProfitLossPercent()),
                        String.format("%.1f", profile.getRiskSettings().getStopLossPercent()));
            } else if (finalAccount.getProfitLossPercent() >= profile.getRiskSettings().getTakeProfitPercent()) {
                logger.info("\n🎉 익절 기준 도달! ({}% >= {}%)", 
                        String.format("%.2f", finalAccount.getProfitLossPercent()),
                        String.format("%.1f", profile.getRiskSettings().getTakeProfitPercent()));
            }
            
            logger.info("\n" + "=".repeat(80));
            logger.info("테스트 완료!");
            logger.info("=".repeat(80));
            
        } catch (Exception e) {
            logger.error("테스트 중 오류 발생", e);
        }
    }
    
    private static void printAccountInfo(AccountInfo account) {
        logger.info("  총 잔고: ${}", String.format("%.2f", account.getTotalBalance()));
        logger.info("  사용 가능 잔고: ${}", String.format("%.2f", account.getAvailableBalance()));
        logger.info("  보유 BTC: {}", String.format("%.6f", account.getBtcHolding()));
        logger.info("  BTC 가치: ${}", String.format("%.2f", account.getBtcValue()));
        logger.info("  현재 손익률: {}%", String.format("%.2f", account.getProfitLossPercent()));
    }
}

