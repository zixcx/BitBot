package com.bitbot.server.controller;

import com.bitbot.monitoring.NotificationService;
import com.bitbot.server.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    /**
     * 최근 알림 조회
     * GET /api/notifications?limit=50
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotifications(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<NotificationService.Notification> notifications = 
                    NotificationService.getInstance().getRecentNotifications(limit);
            
            // DTO로 변환
            List<Map<String, Object>> result = notifications.stream()
                    .map(notif -> {
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("type", notif.getType().name());
                        map.put("title", notif.getTitle());
                        map.put("message", notif.getMessage());
                        map.put("level", notif.getLevel().name());
                        map.put("timestamp", notif.getTimestamp().toString());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            logger.error("알림 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error("알림 조회 실패: " + e.getMessage()));
        }
    }
}

