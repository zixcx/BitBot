package com.bitbot.indicators;

import com.bitbot.models.MarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 기술적 지표 계산 유틸리티
 * RSI, MACD, Bollinger Bands, Moving Averages 등을 계산
 */
public class TechnicalIndicators {
    
    private static final Logger logger = LoggerFactory.getLogger(TechnicalIndicators.class);
    
    /**
     * RSI (Relative Strength Index) 계산
     * @param data 시장 데이터 리스트
     * @param period RSI 기간 (일반적으로 14)
     */
    public static void calculateRSI(List<MarketData> data, int period) {
        if (data.size() < period + 1) {
            logger.warn("RSI 계산을 위한 데이터 부족: {} < {}", data.size(), period + 1);
            return;
        }
        
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();
        
        // 가격 변화 계산
        for (int i = 1; i < data.size(); i++) {
            double change = data.get(i).getClose() - data.get(i - 1).getClose();
            gains.add(change > 0 ? change : 0);
            losses.add(change < 0 ? -change : 0);
        }
        
        // RSI 계산
        for (int i = period; i < data.size(); i++) {
            double avgGain = gains.subList(i - period, i).stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
            double avgLoss = losses.subList(i - period, i).stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
            
            double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
            double rsi = 100 - (100 / (1 + rs));
            
            data.get(i).setRsi(rsi);
        }
    }
    
    /**
     * 단순 이동평균 (SMA - Simple Moving Average) 계산
     * @param data 시장 데이터 리스트
     * @param shortPeriod 단기 이평선 기간 (예: 20)
     * @param longPeriod 장기 이평선 기간 (예: 60)
     */
    public static void calculateMovingAverages(List<MarketData> data, int shortPeriod, int longPeriod) {
        // 단기 이동평균
        for (int i = shortPeriod - 1; i < data.size(); i++) {
            double sum = 0;
            for (int j = 0; j < shortPeriod; j++) {
                sum += data.get(i - j).getClose();
            }
            data.get(i).setMaShort(sum / shortPeriod);
        }
        
        // 장기 이동평균
        for (int i = longPeriod - 1; i < data.size(); i++) {
            double sum = 0;
            for (int j = 0; j < longPeriod; j++) {
                sum += data.get(i - j).getClose();
            }
            data.get(i).setMaLong(sum / longPeriod);
        }
    }
    
    /**
     * MACD (Moving Average Convergence Divergence) 계산
     * @param data 시장 데이터 리스트
     * @param fastPeriod 빠른 EMA 기간 (일반적으로 12)
     * @param slowPeriod 느린 EMA 기간 (일반적으로 26)
     * @param signalPeriod 시그널 기간 (일반적으로 9)
     */
    public static void calculateMACD(List<MarketData> data, int fastPeriod, int slowPeriod, int signalPeriod) {
        if (data.size() < slowPeriod + signalPeriod) {
            logger.warn("MACD 계산을 위한 데이터 부족");
            return;
        }
        
        // EMA 계산
        List<Double> fastEMA = calculateEMA(data, fastPeriod);
        List<Double> slowEMA = calculateEMA(data, slowPeriod);
        
        // MACD 라인 = Fast EMA - Slow EMA
        List<Double> macdLine = new ArrayList<>();
        for (int i = 0; i < slowEMA.size(); i++) {
            macdLine.add(fastEMA.get(i + fastEMA.size() - slowEMA.size()) - slowEMA.get(i));
        }
        
        // Signal 라인 = MACD의 EMA
        List<Double> signalLine = calculateEMAFromValues(macdLine, signalPeriod);
        
        // 데이터에 MACD 값 설정
        int startIndex = data.size() - signalLine.size();
        for (int i = 0; i < signalLine.size(); i++) {
            data.get(startIndex + i).setMacd(macdLine.get(macdLine.size() - signalLine.size() + i));
            data.get(startIndex + i).setMacdSignal(signalLine.get(i));
        }
    }
    
    /**
     * 볼린저 밴드 (Bollinger Bands) 계산
     * @param data 시장 데이터 리스트
     * @param period 기간 (일반적으로 20)
     * @param stdDev 표준편차 배수 (일반적으로 2)
     */
    public static void calculateBollingerBands(List<MarketData> data, int period, double stdDev) {
        for (int i = period - 1; i < data.size(); i++) {
            // 중간선 (SMA)
            double sum = 0;
            for (int j = 0; j < period; j++) {
                sum += data.get(i - j).getClose();
            }
            double sma = sum / period;
            
            // 표준편차 계산
            double variance = 0;
            for (int j = 0; j < period; j++) {
                double diff = data.get(i - j).getClose() - sma;
                variance += diff * diff;
            }
            double sd = Math.sqrt(variance / period);
            
            // 볼린저 밴드 설정
            data.get(i).setBollingerMiddle(sma);
            data.get(i).setBollingerUpper(sma + (stdDev * sd));
            data.get(i).setBollingerLower(sma - (stdDev * sd));
        }
    }
    
    /**
     * EMA (Exponential Moving Average) 계산
     */
    private static List<Double> calculateEMA(List<MarketData> data, int period) {
        List<Double> ema = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        
        // 첫 EMA는 SMA로 시작
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += data.get(i).getClose();
        }
        ema.add(sum / period);
        
        // EMA 계산
        for (int i = period; i < data.size(); i++) {
            double currentEMA = (data.get(i).getClose() - ema.get(ema.size() - 1)) * multiplier + ema.get(ema.size() - 1);
            ema.add(currentEMA);
        }
        
        return ema;
    }
    
    /**
     * 값 리스트로부터 EMA 계산
     */
    private static List<Double> calculateEMAFromValues(List<Double> values, int period) {
        List<Double> ema = new ArrayList<>();
        double multiplier = 2.0 / (period + 1);
        
        // 첫 EMA는 SMA로 시작
        double sum = 0;
        for (int i = 0; i < period && i < values.size(); i++) {
            sum += values.get(i);
        }
        ema.add(sum / Math.min(period, values.size()));
        
        // EMA 계산
        for (int i = period; i < values.size(); i++) {
            double currentEMA = (values.get(i) - ema.get(ema.size() - 1)) * multiplier + ema.get(ema.size() - 1);
            ema.add(currentEMA);
        }
        
        return ema;
    }
    
    /**
     * 모든 기술 지표를 한 번에 계산
     * @param data 시장 데이터 리스트 (최소 200개 권장)
     */
    public static void calculateAllIndicators(List<MarketData> data) {
        if (data.isEmpty()) {
            logger.warn("계산할 데이터가 없습니다");
            return;
        }
        
        logger.debug("기술 지표 계산 시작: {} 개 데이터", data.size());
        
        // RSI (14)
        calculateRSI(data, 14);
        
        // 이동평균선 (20, 60)
        calculateMovingAverages(data, 20, 60);
        
        // MACD (12, 26, 9)
        calculateMACD(data, 12, 26, 9);
        
        // 볼린저 밴드 (20, 2.0)
        calculateBollingerBands(data, 20, 2.0);
        
        logger.debug("기술 지표 계산 완료");
    }
    
    /**
     * 골든 크로스 확인 (단기 이평선이 장기 이평선을 상향 돌파)
     */
    public static boolean isGoldenCross(MarketData current, MarketData previous) {
        if (current.getMaShort() == null || current.getMaLong() == null ||
            previous.getMaShort() == null || previous.getMaLong() == null) {
            return false;
        }
        
        return previous.getMaShort() <= previous.getMaLong() && 
               current.getMaShort() > current.getMaLong();
    }
    
    /**
     * 데드 크로스 확인 (단기 이평선이 장기 이평선을 하향 돌파)
     */
    public static boolean isDeadCross(MarketData current, MarketData previous) {
        if (current.getMaShort() == null || current.getMaLong() == null ||
            previous.getMaShort() == null || previous.getMaLong() == null) {
            return false;
        }
        
        return previous.getMaShort() >= previous.getMaLong() && 
               current.getMaShort() < current.getMaLong();
    }
}


