package com.bitbot.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 알림 서비스
 * 중요한 이벤트(손절, 익절, 에러 등) 발생 시 알림 전송
 * 
 * 현재는 로그 기반 알림만 지원 (향후 이메일, 슬랙, 텔레그램 등 확장 가능)
 */
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 싱글톤 인스턴스
    private static final NotificationService instance = new NotificationService();
    
    // 알림 리스너 목록 (향후 확장용)
    private final List<NotificationListener> listeners = new CopyOnWriteArrayList<>();
    
    // 알림 히스토리 (최근 100개)
    private final List<Notification> notificationHistory = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY = 100;
    
    private NotificationService() {
        // 싱글톤
    }
    
    /**
     * 싱글톤 인스턴스 반환
     */
    public static NotificationService getInstance() {
        return instance;
    }
    
    /**
     * 손절 실행 알림
     * @param lossPercent 손실률 (%)
     * @param currentBalance 현재 잔고
     * @param stopLossPrice 손절 가격
     */
    public void notifyStopLoss(double lossPercent, double currentBalance, double stopLossPrice) {
        String message = String.format(
            "🚨 [긴급] 손절 실행\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "⏰ 시간: %s\n" +
            "📉 손실률: %.2f%%\n" +
            "💰 현재 잔고: $%.2f\n" +
            "💵 손절 가격: $%.2f\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            LocalDateTime.now().format(FORMATTER),
            lossPercent,
            currentBalance,
            stopLossPrice
        );
        
        sendNotification(NotificationType.STOP_LOSS, "손절 실행", message, NotificationLevel.CRITICAL);
    }
    
    /**
     * 익절 실행 알림
     * @param profitPercent 수익률 (%)
     * @param currentBalance 현재 잔고
     * @param takeProfitPrice 익절 가격
     */
    public void notifyTakeProfit(double profitPercent, double currentBalance, double takeProfitPrice) {
        String message = String.format(
            "🎉 [수익] 익절 실행\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "⏰ 시간: %s\n" +
            "📈 수익률: +%.2f%%\n" +
            "💰 현재 잔고: $%.2f\n" +
            "💵 익절 가격: $%.2f\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            LocalDateTime.now().format(FORMATTER),
            profitPercent,
            currentBalance,
            takeProfitPrice
        );
        
        sendNotification(NotificationType.TAKE_PROFIT, "익절 실행", message, NotificationLevel.INFO);
    }
    
    /**
     * 거래 실행 알림
     * @param orderType 주문 타입 (매수/매도)
     * @param quantity 수량
     * @param price 가격
     * @param totalCost 총 비용
     */
    public void notifyTradeExecution(String orderType, double quantity, double price, double totalCost) {
        String message = String.format(
            "📊 [거래 실행] %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "⏰ 시간: %s\n" +
            "📦 수량: %.8f BTC\n" +
            "💵 가격: $%.2f\n" +
            "💰 총 비용: $%.2f\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            orderType,
            LocalDateTime.now().format(FORMATTER),
            quantity,
            price,
            totalCost
        );
        
        sendNotification(NotificationType.TRADE_EXECUTION, "거래 실행", message, NotificationLevel.INFO);
    }
    
    /**
     * 시스템 에러 알림
     * @param errorType 에러 타입
     * @param errorMessage 에러 메시지
     * @param exception 예외 객체 (선택사항)
     */
    public void notifyError(String errorType, String errorMessage, Throwable exception) {
        StringBuilder message = new StringBuilder();
        message.append(String.format(
            "❌ [에러] %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "⏰ 시간: %s\n" +
            "🔴 에러 타입: %s\n" +
            "📝 메시지: %s\n",
            errorType,
            LocalDateTime.now().format(FORMATTER),
            errorType,
            errorMessage
        ));
        
        if (exception != null) {
            message.append(String.format("📍 예외: %s\n", exception.getClass().getSimpleName()));
            if (exception.getMessage() != null) {
                message.append(String.format("💬 상세: %s\n", exception.getMessage()));
            }
        }
        
        message.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        sendNotification(NotificationType.ERROR, errorType, message.toString(), NotificationLevel.ERROR);
    }
    
    /**
     * 시스템 상태 알림
     * @param status 상태 메시지
     * @param details 상세 정보
     */
    public void notifySystemStatus(String status, String details) {
        String message = String.format(
            "ℹ️ [시스템 상태] %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "⏰ 시간: %s\n" +
            "📋 상태: %s\n" +
            "📝 상세: %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            status,
            LocalDateTime.now().format(FORMATTER),
            status,
            details
        );
        
        sendNotification(NotificationType.SYSTEM_STATUS, status, message, NotificationLevel.INFO);
    }
    
    /**
     * 경고 알림
     * @param warningType 경고 타입
     * @param warningMessage 경고 메시지
     */
    public void notifyWarning(String warningType, String warningMessage) {
        String message = String.format(
            "⚠️ [경고] %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "⏰ 시간: %s\n" +
            "⚠️ 경고 타입: %s\n" +
            "📝 메시지: %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            warningType,
            LocalDateTime.now().format(FORMATTER),
            warningType,
            warningMessage
        );
        
        sendNotification(NotificationType.WARNING, warningType, message, NotificationLevel.WARNING);
    }
    
    /**
     * 알림 전송 (내부 메서드)
     */
    private void sendNotification(NotificationType type, String title, String message, NotificationLevel level) {
        Notification notification = new Notification(type, title, message, level, LocalDateTime.now());
        
        // 히스토리에 추가
        synchronized (notificationHistory) {
            notificationHistory.add(notification);
            if (notificationHistory.size() > MAX_HISTORY) {
                notificationHistory.remove(0);
            }
        }
        
        // 로그 출력
        switch (level) {
            case CRITICAL:
                logger.error("\n{}", message);
                break;
            case ERROR:
                logger.error("\n{}", message);
                break;
            case WARNING:
                logger.warn("\n{}", message);
                break;
            case INFO:
            default:
                logger.info("\n{}", message);
                break;
        }
        
        // 리스너에게 알림 (향후 확장용)
        for (NotificationListener listener : listeners) {
            try {
                listener.onNotification(notification);
            } catch (Exception e) {
                logger.error("알림 리스너 실행 실패", e);
            }
        }
    }
    
    /**
     * 알림 리스너 추가 (향후 확장용)
     */
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 알림 리스너 제거
     */
    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 최근 알림 히스토리 조회
     * @param limit 최대 개수
     * @return 알림 목록
     */
    public List<Notification> getRecentNotifications(int limit) {
        synchronized (notificationHistory) {
            int size = notificationHistory.size();
            int start = Math.max(0, size - limit);
            return new ArrayList<>(notificationHistory.subList(start, size));
        }
    }
    
    /**
     * 알림 타입
     */
    public enum NotificationType {
        STOP_LOSS,
        TAKE_PROFIT,
        TRADE_EXECUTION,
        ERROR,
        WARNING,
        SYSTEM_STATUS
    }
    
    /**
     * 알림 레벨
     */
    public enum NotificationLevel {
        CRITICAL,  // 긴급 (손절 등)
        ERROR,     // 에러
        WARNING,   // 경고
        INFO       // 정보
    }
    
    /**
     * 알림 객체
     */
    public static class Notification {
        private final NotificationType type;
        private final String title;
        private final String message;
        private final NotificationLevel level;
        private final LocalDateTime timestamp;
        
        public Notification(NotificationType type, String title, String message, 
                          NotificationLevel level, LocalDateTime timestamp) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.level = level;
            this.timestamp = timestamp;
        }
        
        public NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public NotificationLevel getLevel() { return level; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    /**
     * 알림 리스너 인터페이스 (향후 확장용)
     */
    public interface NotificationListener {
        void onNotification(Notification notification);
    }
}

