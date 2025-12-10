package com.bitbot.server.controller;

import com.bitbot.AutoTradingService;
import com.bitbot.TradingEngine;
import com.bitbot.database.UserProfileRepository;
import com.bitbot.models.TradingStrategy;
import com.bitbot.models.UserProfile;
import com.bitbot.server.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 자동 거래 제어 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/trading")
@CrossOrigin(origins = "*")
public class TradingControlController {
    
    private static final Logger logger = LoggerFactory.getLogger(TradingControlController.class);
    
    // 싱글톤 인스턴스 (실제로는 Spring Bean으로 관리해야 함)
    private static AutoTradingService autoTradingService;
    private static TradingEngine tradingEngine;
    
    // 기본 사용자 ID
    private static final Integer DEFAULT_USER_ID = 1;
    
    /**
     * 자동 거래 서비스 초기화 (최초 호출 시)
     */
    private synchronized void initializeService() {
        if (autoTradingService == null) {
            tradingEngine = new TradingEngine();
            tradingEngine.setUserId(DEFAULT_USER_ID);
            
            // 사용자 프로필에서 전략 가져오기
            UserProfileRepository profileRepo = new UserProfileRepository();
            UserProfile profile = profileRepo.findByUserId(DEFAULT_USER_ID);
            TradingStrategy strategy = profile != null ? profile.getTradingStrategy() : null;
            
            autoTradingService = new AutoTradingService(tradingEngine, strategy);
            autoTradingService.registerShutdownHook();
        }
    }
    
    /**
     * 자동 거래 시작
     * POST /api/trading/start
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startTrading() {
        try {
            initializeService();
            
            if (autoTradingService.isRunning()) {
                return ResponseEntity.ok(ApiResponse.error("자동 거래가 이미 실행 중입니다."));
            }
            
            autoTradingService.start();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "started");
            result.put("strategy", autoTradingService.getStrategy() != null ? 
                    autoTradingService.getStrategy().name() : null);
            result.put("intervalMinutes", autoTradingService.getIntervalMinutes());
            
            return ResponseEntity.ok(ApiResponse.success("자동 거래 시작", result));
            
        } catch (Exception e) {
            logger.error("자동 거래 시작 실패", e);
            return ResponseEntity.ok(ApiResponse.error("자동 거래 시작 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 자동 거래 중지
     * POST /api/trading/stop
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stopTrading() {
        try {
            if (autoTradingService == null || !autoTradingService.isRunning()) {
                return ResponseEntity.ok(ApiResponse.error("자동 거래가 실행 중이 아닙니다."));
            }
            
            autoTradingService.stop();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "stopped");
            
            return ResponseEntity.ok(ApiResponse.success("자동 거래 중지", result));
            
        } catch (Exception e) {
            logger.error("자동 거래 중지 실패", e);
            return ResponseEntity.ok(ApiResponse.error("자동 거래 중지 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 자동 거래 상태 조회
     * GET /api/trading/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTradingStatus() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            if (autoTradingService == null) {
                result.put("running", false);
                result.put("status", "not_initialized");
            } else {
                result.put("running", autoTradingService.isRunning());
                result.put("status", autoTradingService.isRunning() ? "running" : "stopped");
                result.put("strategy", autoTradingService.getStrategy() != null ? 
                        autoTradingService.getStrategy().name() : null);
                result.put("intervalMinutes", autoTradingService.getIntervalMinutes());
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            logger.error("자동 거래 상태 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("자동 거래 상태 조회 실패: " + e.getMessage()));
        }
    }
}

