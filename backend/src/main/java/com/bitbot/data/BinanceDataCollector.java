package com.bitbot.data;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.bitbot.cache.MarketDataCache;
import com.bitbot.models.AccountInfo;
import com.bitbot.models.MarketData;
import com.bitbot.utils.ConfigLoader;
import com.bitbot.utils.RateLimiter;
import com.bitbot.utils.RetryUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Binance API 데이터 수집기
 * 실시간 시세, 계좌 정보 등을 수집
 */
public class BinanceDataCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(BinanceDataCollector.class);
    private final SpotClient client;
    private final ObjectMapper objectMapper;
    private final boolean isTestnet;
    
    // Binance API Rate Limiter (1200 requests per minute)
    private static final RateLimiter rateLimiter = RateLimiter.getBinanceRateLimiter();
    
    public BinanceDataCollector() {
        this.isTestnet = ConfigLoader.isTestnet();
        this.objectMapper = new ObjectMapper();
        
        String apiKey = ConfigLoader.getBinanceApiKey();
        String secretKey = ConfigLoader.getBinanceSecretKey();
        
        if (isTestnet) {
            logger.info("Binance Testnet 모드로 초기화");
            this.client = new SpotClientImpl(apiKey, secretKey, "https://testnet.binance.vision");
        } else {
            logger.info("Binance 실거래 모드로 초기화");
            this.client = new SpotClientImpl(apiKey, secretKey);
        }
    }
    
    /**
     * 현재 가격 조회 (재시도 로직 포함)
     */
    public double getCurrentPrice(String symbol) {
        try {
            // Rate limit 확인 및 대기
            rateLimiter.acquire();
            
            return RetryUtil.retryIfRetryable(
                () -> {
                    try {
                        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                        parameters.put("symbol", symbol);
                        
                        String response = client.createMarket().tickerSymbol(parameters);
                        JsonNode json = objectMapper.readTree(response);
                        
                        return json.get("price").asDouble();
                    } catch (Exception e) {
                        throw new RuntimeException("가격 조회 실패: " + e.getMessage(), e);
                    }
                },
                3,  // 최대 3회 재시도
                1000  // 초기 지연 1초
            );
        } catch (Exception e) {
            logger.error("현재 가격 조회 실패 (재시도 후): {}", symbol, e);
            return 0.0;  // 실패 시 0.0 반환
        }
    }
    
    /**
     * 캔들스틱 데이터 조회 (OHLCV) - 캐싱 및 재시도 로직 포함
     * @param symbol 심볼 (예: BTCUSDT)
     * @param interval 시간 간격 (예: 15m, 1h, 1d)
     * @param limit 데이터 개수 (최대 1000)
     */
    public List<MarketData> getKlines(String symbol, String interval, int limit) {
        // 캐시에서 조회 또는 API 호출
        return MarketDataCache.getOrFetch(symbol, interval, limit, () -> {
            try {
                // Rate limit 확인 및 대기
                rateLimiter.acquire();
                
                return RetryUtil.retryIfRetryable(
                    () -> {
                        try {
                            List<MarketData> marketDataList = new ArrayList<>();
                            
                            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                            parameters.put("symbol", symbol);
                            parameters.put("interval", interval);
                            parameters.put("limit", limit);
                            
                            String response = client.createMarket().klines(parameters);
                            JsonNode jsonArray = objectMapper.readTree(response);
                            
                            for (JsonNode candle : jsonArray) {
                                MarketData data = new MarketData();
                                
                                // [0] Open time
                                long timestamp = candle.get(0).asLong();
                                data.setTimestamp(LocalDateTime.ofInstant(
                                        Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
                                
                                // [1] Open
                                data.setOpen(candle.get(1).asDouble());
                                // [2] High
                                data.setHigh(candle.get(2).asDouble());
                                // [3] Low
                                data.setLow(candle.get(3).asDouble());
                                // [4] Close
                                data.setClose(candle.get(4).asDouble());
                                // [5] Volume
                                data.setVolume(candle.get(5).asDouble());
                                
                                // [7] Quote asset volume
                                data.setQuoteVolume(candle.get(7).asDouble());
                                // [8] Number of trades
                                data.setTradeCount(candle.get(8).asInt());
                                // [9] Taker buy base asset volume
                                data.setTakerBuyVolume(candle.get(9).asDouble());
                                // [10] Taker buy quote asset volume
                                data.setTakerBuyQuote(candle.get(10).asDouble());
                                
                                marketDataList.add(data);
                            }
                            
                            logger.debug("캔들스틱 데이터 {}개 조회 완료: {} {}", marketDataList.size(), symbol, interval);
                            return marketDataList;
                        } catch (Exception e) {
                            throw new RuntimeException("캔들스틱 데이터 조회 실패: " + e.getMessage(), e);
                        }
                    },
                    3,  // 최대 3회 재시도
                    1000  // 초기 지연 1초
                );
            } catch (Exception e) {
                logger.error("캔들스틱 데이터 조회 실패 (재시도 후)", e);
                // 예외를 던지지 않고 빈 리스트 반환
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * 계좌 정보 조회 (재시도 로직 포함)
     */
    public AccountInfo getAccountInfo() {
        AccountInfo accountInfo = new AccountInfo();
        
        try {
            // 재시도 가능한 API 호출
            JsonNode json = RetryUtil.retryIfRetryable(
                () -> {
                    try {
                        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                        String response = client.createTrade().account(parameters);
                        return objectMapper.readTree(response);
                    } catch (Exception e) {
                        throw new RuntimeException("계좌 정보 조회 실패: " + e.getMessage(), e);
                    }
                },
                3,  // 최대 3회 재시도
                1000  // 초기 지연 1초
            );
            
            JsonNode balances = json.get("balances");
            if (balances == null || !balances.isArray()) {
                logger.warn("계좌 정보 응답에 balances가 없습니다. 시뮬레이션 모드로 기본값 사용");
                // 시뮬레이션 모드: 기본값 설정
                accountInfo.setAvailableBalance(10000.0);
                accountInfo.setTotalBalance(10000.0);
                accountInfo.setBtcHolding(0.0);
                accountInfo.setBtcValue(0.0);
                accountInfo.setInvestedAmount(0.0);
                return accountInfo;
            }
            
            double usdtBalance = 0.0;
            double btcBalance = 0.0;
            
            for (JsonNode balance : balances) {
                String asset = balance.get("asset").asText();
                double free = balance.get("free").asDouble();
                double locked = balance.get("locked").asDouble();
                double total = free + locked;
                
                if ("USDT".equals(asset)) {
                    usdtBalance = total;
                } else if ("BTC".equals(asset)) {
                    btcBalance = total;
                }
            }
            
            // BTC 현재 가격 조회
            double btcPrice = getCurrentPrice("BTCUSDT");
            if (btcPrice <= 0) {
                logger.warn("BTC 가격 조회 실패. 기본값 사용: $50,000");
                btcPrice = 50000.0;
            }
            
            double btcValue = btcBalance * btcPrice;
            
            accountInfo.setAvailableBalance(usdtBalance);
            accountInfo.setTotalBalance(usdtBalance + btcValue);
            accountInfo.setBtcHolding(btcBalance);
            accountInfo.setBtcValue(btcValue);
            accountInfo.setInvestedAmount(btcValue);
            
            logger.info("계좌 정보 조회 완료: {}", accountInfo);
            
        } catch (com.binance.connector.client.exceptions.BinanceClientException e) {
            // Binance API 권한 오류
            if (e.getMessage() != null && e.getMessage().contains("permissions")) {
                logger.warn("계좌 정보 조회 권한 없음: Binance API 키에 'USER_DATA' 권한이 필요합니다.");
                logger.warn("시뮬레이션 모드로 전환: 데이터베이스 거래 내역 기반으로 계산");
            } else {
                logger.error("계좌 정보 조회 실패: {}", e.getMessage());
                logger.warn("시뮬레이션 모드로 전환: 데이터베이스 거래 내역 기반으로 계산");
            }
            
            // 오류 발생 시 데이터베이스 거래 내역 기반으로 계산
            accountInfo = calculateAccountFromTrades();
            
        } catch (Exception e) {
            logger.error("계좌 정보 조회 실패: {}", e.getMessage());
            logger.warn("시뮬레이션 모드로 전환: 데이터베이스 거래 내역 기반으로 계산");
            
            // 오류 발생 시 데이터베이스 거래 내역 기반으로 계산
            accountInfo = calculateAccountFromTrades();
        }
        
        return accountInfo;
    }
    
    /**
     * 24시간 거래량 및 가격 변동 정보
     */
    public Map<String, Double> get24hrStats(String symbol) {
        Map<String, Double> stats = new LinkedHashMap<>();
        
        try {
            // Rate limit 확인 및 대기
            rateLimiter.acquire();
            
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", symbol);
            
            String response = client.createMarket().ticker24H(parameters);
            JsonNode json = objectMapper.readTree(response);
            
            stats.put("priceChange", json.get("priceChange").asDouble());
            stats.put("priceChangePercent", json.get("priceChangePercent").asDouble());
            stats.put("highPrice", json.get("highPrice").asDouble());
            stats.put("lowPrice", json.get("lowPrice").asDouble());
            stats.put("volume", json.get("volume").asDouble());
            stats.put("quoteVolume", json.get("quoteVolume").asDouble());
            
        } catch (Exception e) {
            logger.error("24시간 통계 조회 실패", e);
        }
        
        return stats;
    }
    
    /**
     * 서버 시간 확인 (연결 테스트용)
     */
    public boolean testConnection() {
        try {
            // Rate limit 확인 및 대기
            rateLimiter.acquire();
            
            String response = client.createMarket().time();
            JsonNode json = objectMapper.readTree(response);
            long serverTime = json.get("serverTime").asLong();
            
            logger.info("Binance 서버 연결 성공. 서버 시간: {}", 
                    Instant.ofEpochMilli(serverTime));
            return true;
            
        } catch (Exception e) {
            logger.error("Binance 서버 연결 실패", e);
            return false;
        }
    }
    
    /**
     * 데이터베이스 거래 내역 기반으로 계좌 정보 계산 (시뮬레이션 모드)
     */
    private AccountInfo calculateAccountFromTrades() {
        AccountInfo accountInfo = new AccountInfo();
        
        try {
            // 거래 내역 조회
            com.bitbot.database.TradeRepository tradeRepo = new com.bitbot.database.TradeRepository();
            java.util.List<com.bitbot.models.TradeOrder> trades = tradeRepo.findRecentTrades("1", 1000);
            
            double initialBalance = 10000.0;  // 초기 잔고
            double usdtBalance = initialBalance;
            double btcHolding = 0.0;
            
            // 모든 거래 내역을 순회하며 계산
            for (com.bitbot.models.TradeOrder trade : trades) {
                if (trade.getStatus() == com.bitbot.models.TradeOrder.OrderStatus.FILLED) {
                    if (trade.getType() == com.bitbot.models.TradeOrder.OrderType.MARKET_BUY) {
                        // 매수: USDT 감소, BTC 증가
                        double cost = trade.getTotalCost();
                        usdtBalance -= cost;
                        btcHolding += trade.getQuantity();
                    } else if (trade.getType() == com.bitbot.models.TradeOrder.OrderType.MARKET_SELL) {
                        // 매도: USDT 증가, BTC 감소
                        double revenue = trade.getTotalCost();
                        usdtBalance += revenue;
                        btcHolding -= trade.getQuantity();
                    }
                }
            }
            
            // BTC 현재 가격 조회
            double btcPrice = getCurrentPrice("BTCUSDT");
            if (btcPrice <= 0) {
                logger.warn("BTC 가격 조회 실패. 기본값 사용: $50,000");
                btcPrice = 50000.0;
            }
            
            double btcValue = btcHolding * btcPrice;
            double totalBalance = usdtBalance + btcValue;
            
            accountInfo.setAvailableBalance(usdtBalance);
            accountInfo.setTotalBalance(totalBalance);
            accountInfo.setBtcHolding(btcHolding);
            accountInfo.setBtcValue(btcValue);
            accountInfo.setInvestedAmount(btcValue);
            
            // 손익률 계산
            double profitLoss = totalBalance - initialBalance;
            double profitLossPercent = (profitLoss / initialBalance) * 100.0;
            // AccountInfo는 profitLoss 필드가 없으므로 profitLossPercent만 설정
            accountInfo.setProfitLossPercent(profitLossPercent);
            
            // 레버리지 거래 고려 (데이터베이스에서 레버리지 정보 확인)
            // 현재는 시뮬레이션이므로 레버리지 포지션도 일반 포지션으로 계산
            logger.info("[시뮬레이션] 데이터베이스 기반 계좌 정보: USDT={}, BTC={}, 총액={}, 손익={}%",
                    String.format("%.2f", usdtBalance),
                    String.format("%.6f", btcHolding),
                    String.format("%.2f", totalBalance),
                    String.format("%.2f", profitLossPercent));
            
        } catch (Exception e) {
            logger.error("데이터베이스 기반 계좌 계산 실패", e);
            // 최종 폴백: 기본값
            accountInfo.setAvailableBalance(10000.0);
            accountInfo.setTotalBalance(10000.0);
            accountInfo.setBtcHolding(0.0);
            accountInfo.setBtcValue(0.0);
            accountInfo.setInvestedAmount(0.0);
        }
        
        return accountInfo;
    }
}


