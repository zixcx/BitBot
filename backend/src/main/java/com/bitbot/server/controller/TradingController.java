package com.bitbot.server.controller;

import com.bitbot.data.BinanceDataCollector;
import com.bitbot.database.TradeRepository;
import com.bitbot.database.TradeLogRepository;
import com.bitbot.database.UserProfileRepository;
import com.bitbot.models.AccountInfo;
import com.bitbot.models.TradeOrder;
import com.bitbot.models.UserProfile;
import com.bitbot.server.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 거래 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TradingController {
    
    private static final Logger logger = LoggerFactory.getLogger(TradingController.class);
    
    private final TradeRepository tradeRepository;
    private final TradeLogRepository tradeLogRepository;
    private final UserProfileRepository profileRepository;
    private final BinanceDataCollector dataCollector;
    
    // 기본 사용자 ID (실제로는 인증 시스템에서 가져옴)
    private static final Integer DEFAULT_USER_ID = 1;
    
    public TradingController() {
        this.tradeRepository = new TradeRepository();
        this.tradeLogRepository = new TradeLogRepository();
        this.profileRepository = new UserProfileRepository();
        this.dataCollector = new BinanceDataCollector();
    }
    
    /**
     * 거래 내역 조회
     * GET /api/trades
     */
    @GetMapping("/trades")
    public ResponseEntity<ApiResponse<List<TradeOrder>>> getTrades(
            @RequestParam(required = false, defaultValue = "50") int limit) {
        try {
            List<TradeOrder> trades = tradeRepository.findRecentTrades(DEFAULT_USER_ID.toString(), limit);
            return ResponseEntity.ok(ApiResponse.success(trades));
        } catch (Exception e) {
            logger.error("거래 내역 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("거래 내역 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 계좌 정보 조회
     * GET /api/account
     */
    @GetMapping("/account")
    public ResponseEntity<ApiResponse<AccountInfo>> getAccount() {
        try {
            AccountInfo accountInfo = dataCollector.getAccountInfo();
            return ResponseEntity.ok(ApiResponse.success(accountInfo));
        } catch (Exception e) {
            logger.error("계좌 정보 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("계좌 정보 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 사용자 프로필 조회
     * GET /api/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfile>> getProfile() {
        try {
            UserProfile profile = profileRepository.findByUserId(DEFAULT_USER_ID);
            if (profile == null) {
                return ResponseEntity.ok(ApiResponse.error("프로필이 없습니다. 설문조사를 먼저 완료하세요."));
            }
            return ResponseEntity.ok(ApiResponse.success(profile));
        } catch (Exception e) {
            logger.error("프로필 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("프로필 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 거래 로그 조회 (AI 판단 기록)
     * GET /api/trade-logs
     */
    @GetMapping("/trade-logs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTradeLogs(
            @RequestParam(required = false, defaultValue = "50") int limit) {
        try {
            List<Map<String, Object>> logs = tradeLogRepository.findRecentLogs(DEFAULT_USER_ID, limit);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            logger.error("거래 로그 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("거래 로그 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 서버 상태 확인
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "status", "UP",
            "service", "BitBot Trading Server"
        )));
    }
}

