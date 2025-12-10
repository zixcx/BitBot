package com.bitbot.trading;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.bitbot.exceptions.OrderExecutionException;
import com.bitbot.models.TradeOrder;
import com.bitbot.models.TradingDecision;
import com.bitbot.utils.ConfigLoader;
import com.bitbot.utils.RateLimiter;
import com.bitbot.utils.ValidationUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

/**
 * 주문 실행 모듈
 * Binance API를 통해 실제 매수/매도 주문 실행
 */
public class OrderExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderExecutor.class);
    
    private final SpotClient client;
    private final ObjectMapper objectMapper;
    private final String tradingMode;
    
    // Binance API Rate Limiter (주문 실행도 같은 제한 적용)
    private static final RateLimiter rateLimiter = RateLimiter.getBinanceRateLimiter();
    
    public OrderExecutor() {
        this.tradingMode = ConfigLoader.getTradingMode();
        this.objectMapper = new ObjectMapper();
        
        String apiKey = ConfigLoader.getBinanceApiKey();
        String secretKey = ConfigLoader.getBinanceSecretKey();
        
        if (ConfigLoader.isTestnet()) {
            logger.info("주문 실행 모듈 초기화: Testnet 모드");
            this.client = new SpotClientImpl(apiKey, secretKey, "https://testnet.binance.vision");
        } else {
            logger.info("주문 실행 모듈 초기화: 실거래 모드");
            this.client = new SpotClientImpl(apiKey, secretKey);
        }
    }
    
    /**
     * 시장가 주문 실행
     * @param decision 거래 결정
     * @param quantity 수량 (BTC)
     * @param leverage 레버리지 배수 (1 = 현물, 3 이상 = 선물)
     * @return 주문 결과
     */
    public TradeOrder executeMarketOrder(TradingDecision decision, double quantity, int leverage) 
            throws OrderExecutionException {
        // 입력값 검증
        ValidationUtil.validateOrderQuantity(quantity);
        ValidationUtil.validateLeverage(leverage, 125);  // 최대 레버리지 125
        
        String symbol = "BTCUSDT";
        
        TradeOrder order = new TradeOrder();
        order.setSymbol(symbol);
        order.setQuantity(quantity);
        order.setDecision(decision);
        order.setReason(decision.getReason());
        order.setLeverage(leverage);
        order.setFuturesTrade(leverage > 1);
        
        // 거래 모드 확인
        if ("SIMULATION".equals(tradingMode)) {
            logger.info("📊 [시뮬레이션 모드] 실제 주문 없이 로깅만 수행");
            return simulateOrder(order, decision, leverage);
        }
        
        // 레버리지 사용 시 선물 거래 (현재는 시뮬레이션만 지원)
        if (leverage > 1) {
            logger.info("⚡ [레버리지 {}배] 선물 거래 시뮬레이션 모드", leverage);
            return simulateLeveragedOrder(order, decision, leverage);
        }
        
        try {
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", symbol);
            parameters.put("quantity", String.format("%.8f", quantity));
            
            String response;
            
            // 매수/매도 구분
            if (decision.getDecision() == TradingDecision.Decision.BUY || 
                decision.getDecision() == TradingDecision.Decision.STRONG_BUY) {
                
                order.setType(TradeOrder.OrderType.MARKET_BUY);
                parameters.put("side", "BUY");
                parameters.put("type", "MARKET");
                
                logger.info("🟢 [매수 주문 실행] {} BTC @ 시장가", quantity);
                // Rate limit 확인 및 대기
                rateLimiter.acquire();
                response = client.createTrade().newOrder(parameters);
                
            } else if (decision.getDecision() == TradingDecision.Decision.SELL || 
                       decision.getDecision() == TradingDecision.Decision.STRONG_SELL) {
                
                order.setType(TradeOrder.OrderType.MARKET_SELL);
                parameters.put("side", "SELL");
                parameters.put("type", "MARKET");
                
                logger.info("🔴 [매도 주문 실행] {} BTC @ 시장가", quantity);
                // Rate limit 확인 및 대기
                rateLimiter.acquire();
                response = client.createTrade().newOrder(parameters);
                
            } else {
                logger.warn("실행할 수 없는 결정: {}", decision.getDecision());
                order.setStatus(TradeOrder.OrderStatus.REJECTED);
                return order;
            }
            
            // 응답 파싱
            JsonNode json = objectMapper.readTree(response);
            
            order.setBinanceOrderId(json.get("orderId").asText());
            order.setExecutedPrice(json.get("fills").get(0).get("price").asDouble());
            order.setPrice(order.getExecutedPrice());
            order.setTotalCost(order.getExecutedPrice() * quantity);
            order.setStatus(TradeOrder.OrderStatus.FILLED);
            order.setExecutedAt(java.time.LocalDateTime.now());
            
            logger.info("✅ 주문 체결 완료: {}", order);
            
        } catch (Exception e) {
            logger.error("주문 실행 실패", e);
            order.setStatus(TradeOrder.OrderStatus.FAILED);
            order.setReason("주문 실패: " + e.getMessage());
            throw new OrderExecutionException("주문 실행 실패: " + e.getMessage(), e);
        }
        
        return order;
    }
    
    /**
     * 레버리지 주문 시뮬레이션 (선물 거래)
     */
    private TradeOrder simulateLeveragedOrder(TradeOrder order, TradingDecision decision, int leverage) {
        try {
            // 현재 시장가 조회
            rateLimiter.acquire(); // Rate limit 확인
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            
            String response = client.createMarket().tickerSymbol(parameters);
            JsonNode json = objectMapper.readTree(response);
            
            double currentPrice = json.get("price").asDouble();
            
            // 레버리지 적용: 실제 투자 금액은 quantity * price / leverage
            double actualInvestment = (order.getQuantity() * currentPrice) / leverage;
            double leveragedPosition = order.getQuantity() * currentPrice;
            
            // 시뮬레이션 결과 설정
            if (decision.getDecision() == TradingDecision.Decision.BUY || 
                decision.getDecision() == TradingDecision.Decision.STRONG_BUY) {
                order.setType(TradeOrder.OrderType.MARKET_BUY);
            } else {
                order.setType(TradeOrder.OrderType.MARKET_SELL);
            }
            
            order.setPrice(currentPrice);
            order.setExecutedPrice(currentPrice);
            order.setTotalCost(leveragedPosition);  // 레버리지 적용된 포지션 크기
            order.setStatus(TradeOrder.OrderStatus.FILLED);
            order.setExecutedAt(java.time.LocalDateTime.now());
            order.setBinanceOrderId("FUTURES-SIM-" + System.currentTimeMillis());
            
            logger.info("⚡ [레버리지 {}배 시뮬레이션] {} {} @ ${:.2f} (포지션: ${:.2f}, 실제 투자: ${:.2f})",
                    leverage,
                    order.isBuyOrder() ? "매수" : "매도",
                    order.getQuantity(),
                    order.getExecutedPrice(),
                    leveragedPosition,
                    actualInvestment);
            
        } catch (Exception e) {
            logger.error("레버리지 주문 시뮬레이션 실패", e);
            order.setStatus(TradeOrder.OrderStatus.FAILED);
        }
        
        return order;
    }
    
    /**
     * 시뮬레이션 모드: 실제 주문 없이 결과만 시뮬레이션 (현물 거래)
     */
    private TradeOrder simulateOrder(TradeOrder order, TradingDecision decision, int leverage) {
        try {
            // 현재 시장가 조회
            rateLimiter.acquire(); // Rate limit 확인
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            
            String response = client.createMarket().tickerSymbol(parameters);
            JsonNode json = objectMapper.readTree(response);
            
            double currentPrice = json.get("price").asDouble();
            
            // 시뮬레이션 결과 설정
            if (decision.getDecision() == TradingDecision.Decision.BUY || 
                decision.getDecision() == TradingDecision.Decision.STRONG_BUY) {
                order.setType(TradeOrder.OrderType.MARKET_BUY);
            } else {
                order.setType(TradeOrder.OrderType.MARKET_SELL);
            }
            
            order.setPrice(currentPrice);
            order.setExecutedPrice(currentPrice);
            order.setTotalCost(currentPrice * order.getQuantity());
            order.setStatus(TradeOrder.OrderStatus.FILLED);
            order.setExecutedAt(java.time.LocalDateTime.now());
            order.setBinanceOrderId("SPOT-SIM-" + System.currentTimeMillis());
            
            logger.info("📊 [현물 시뮬레이션] {} {} @ ${:.2f} (총 ${:.2f})",
                    order.isBuyOrder() ? "매수" : "매도",
                    order.getQuantity(),
                    order.getExecutedPrice(),
                    order.getTotalCost());
            
        } catch (Exception e) {
            logger.error("시뮬레이션 실행 실패", e);
            order.setStatus(TradeOrder.OrderStatus.FAILED);
        }
        
        return order;
    }
    
    /**
     * 주문 가능 여부 확인
     */
    public boolean canPlaceOrder() {
        // 거래 모드 확인
        if ("SIMULATION".equals(tradingMode)) {
            return true;  // 시뮬레이션은 항상 가능
        }
        
        try {
            // Binance 서버 연결 테스트
            rateLimiter.acquire(); // Rate limit 확인
            client.createMarket().time();
            return true;
        } catch (Exception e) {
            logger.error("Binance 서버 연결 실패", e);
            return false;
        }
    }
}


