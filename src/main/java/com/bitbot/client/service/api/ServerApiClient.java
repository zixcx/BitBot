package com.bitbot.client.service.api;

import com.bitbot.client.dto.*;
import com.bitbot.client.model.MarketAnalysis;
import com.bitbot.client.model.TradeDecision;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * BitBot Server API Client
 * Handles communication with Spring Boot backend
 * 
 * Base URL: http://203.234.62.223:8080/api
 */
public class ServerApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerApiClient.class);
    private static final String BASE_URL = "http://203.234.62.223:8080/api";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String sessionToken;

    public ServerApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // ==================== Authentication API ====================

    /**
     * Register a new user
     * POST /api/auth/register
     */
    public CompletableFuture<Boolean> register(String email, String username, String password, 
                                                String binanceApiKey, String binanceSecretKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> body = new HashMap<>();
                body.put("email", email);
                body.put("username", username);
                body.put("password", password);
                if (binanceApiKey != null && !binanceApiKey.isEmpty()) {
                    body.put("binanceApiKey", binanceApiKey);
                }
                if (binanceSecretKey != null && !binanceSecretKey.isEmpty()) {
                    body.put("binanceSecretKey", binanceSecretKey);
                }
                
                String requestBody = objectMapper.writeValueAsString(body);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    ApiResponse<Map> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<Map>>() {}
                    );
                    logger.info("Registration successful");
                    return apiResponse.isSuccess();
                } else {
                    logger.error("Registration failed with HTTP {}: {}", response.statusCode(), response.body());
                    return false;
                }
                
            } catch (Exception e) {
                logger.error("Registration error", e);
                return false;
            }
        });
    }

    /**
     * Login to server
     * POST /api/auth/login
     */
    public CompletableFuture<String> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> body = new HashMap<>();
                body.put("email", email);
                body.put("password", password);
                
                String requestBody = objectMapper.writeValueAsString(body);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<Map> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<Map>>() {}
                    );
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        sessionToken = (String) apiResponse.getData().get("sessionToken");
                        logger.info("Login successful, session token obtained");
                        return sessionToken;
                    } else {
                        String errorMsg = apiResponse.getErrorMessage();
                        logger.error("Login failed: {}", errorMsg);
                        throw new Exception(errorMsg != null ? errorMsg : "Login failed");
                    }
                } else {
                    String errorMsg = "Login failed with HTTP " + response.statusCode();
                    logger.error(errorMsg + ": " + response.body());
                    throw new Exception(errorMsg);
                }
                
            } catch (Exception e) {
                logger.error("Login error", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Logout from server
     * POST /api/auth/logout
     */
    public CompletableFuture<Boolean> logoutFromServer() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/logout"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    sessionToken = null;
                    logger.info("Logged out successfully");
                    return true;
                } else {
                    logger.error("Logout failed with HTTP {}", response.statusCode());
                    return false;
                }
                
            } catch (Exception e) {
                logger.error("Logout error", e);
                return false;
            }
        });
    }

    /**
     * Verify session token
     * GET /api/auth/verify
     */
    public CompletableFuture<Boolean> verifySession() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/verify"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<Map> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<Map>>() {}
                    );
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Boolean valid = (Boolean) apiResponse.getData().get("valid");
                        return valid != null && valid;
                    }
                }
                return false;
                
            } catch (Exception e) {
                logger.error("Session verification error", e);
                return false;
            }
        });
    }

    // ==================== Trade API ====================

    /**
     * Get trade history
     * GET /api/trades
     */
    public CompletableFuture<List<TradeOrderDto>> getTrades(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/trades?limit=" + limit))
                    .header("Authorization", "Bearer " + sessionToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<List<TradeOrderDto>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<List<TradeOrderDto>>>() {}
                    );
                    
                    if (apiResponse.isSuccess()) {
                        logger.info("Fetched {} trades", apiResponse.getData().size());
                        return apiResponse.getData();
                    }
                }
                throw new Exception("Failed to fetch trades: " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error fetching trades", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get account information
     * GET /api/account
     */
    public CompletableFuture<AccountInfoDto> getAccountInfo() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/account"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<AccountInfoDto> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<AccountInfoDto>>() {}
                    );
                    
                    if (apiResponse.isSuccess()) {
                        logger.info("Account info fetched successfully");
                        return apiResponse.getData();
                    }
                }
                throw new Exception("Failed to fetch account info: " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error fetching account info", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get user profile (risk settings)
     * GET /api/profile
     */
    public CompletableFuture<UserProfileDto> getUserProfile() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/profile"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<UserProfileDto> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<UserProfileDto>>() {}
                    );
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        logger.info("Profile loaded: {}", apiResponse.getData().getInvestorType());
                        return apiResponse.getData();
                    } else {
                        // Profile doesn't exist or failed
                        String errorMsg = apiResponse.getErrorMessage();
                        logger.warn("Profile not found or failed: {}", errorMsg);
                        throw new Exception(errorMsg != null ? errorMsg : "프로필이 없습니다");
                    }
                } else if (response.statusCode() == 404) {
                    throw new Exception("프로필이 없습니다. 설문조사를 먼저 완료하세요.");
                }
                throw new Exception("Failed to get profile: HTTP " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error fetching profile", e);
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== Trading Control API ====================

    /**
     * Start automated trading
     * POST /api/trading/start
     */
    public CompletableFuture<Boolean> startTrading() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/trading/start"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    logger.info("Trading started successfully");
                    return true;
                } else {
                    logger.error("Failed to start trading: HTTP {}", response.statusCode());
                    return false;
                }
                
            } catch (Exception e) {
                logger.error("Error starting trading", e);
                return false;
            }
        });
    }

    /**
     * Stop automated trading
     * POST /api/trading/stop
     */
    public CompletableFuture<Boolean> stopTrading() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/trading/stop"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    logger.info("Trading stopped successfully");
                    return true;
                } else {
                    logger.error("Failed to stop trading: HTTP {}", response.statusCode());
                    return false;
                }
                
            } catch (Exception e) {
                logger.error("Error stopping trading", e);
                return false;
            }
        });
    }

    /**
     * Get trading status
     * GET /api/trading/status
     */
    public CompletableFuture<TradingStatusDto> getTradingStatus() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/trading/status"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<TradingStatusDto> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<TradingStatusDto>>() {}
                    );
                    
                    if (apiResponse.isSuccess()) {
                        return apiResponse.getData();
                    }
                }
                throw new Exception("Failed to get trading status: " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error fetching trading status", e);
                throw new RuntimeException(e);
            }
        });
    }


    // ==================== Market Data API ====================

    /**
     * Get current price
     * GET /api/market/price?symbol={symbol}
     */
    public CompletableFuture<MarketPriceDto> getMarketPrice(String symbol) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/market/price?symbol=" + symbol))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<MarketPriceDto> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<MarketPriceDto>>() {}
                    );
                    
                    if (apiResponse.isSuccess()) {
                        return apiResponse.getData();
                    }
                }
                throw new Exception("Failed to get market price: " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error fetching market price", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get 24h statistics
     * GET /api/market/24h-stats?symbol={symbol}
     */
    public CompletableFuture<MarketStatsDto> getMarketStats(String symbol) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/market/24h-stats?symbol=" + symbol))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<MarketStatsDto> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<MarketStatsDto>>() {}
                    );
                    
                    if (apiResponse.isSuccess()) {
                        return apiResponse.getData();
                    }
                }
                throw new Exception("Failed to get market stats: " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error fetching market stats", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get chart data
     * GET /api/market/chart?symbol={symbol}&timeframe={timeframe}&limit={limit}
     */
    public CompletableFuture<List<ChartDataDto>> getChartData(String symbol, String timeframe, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/market/chart?symbol=" + symbol + "&timeframe=" + timeframe + "&limit=" + limit))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<List<ChartDataDto>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<List<ChartDataDto>>>() {}
                    );
                    
                    if (apiResponse.isSuccess()) {
                        return apiResponse.getData();
                    }
                }
                throw new Exception("Failed to get chart data: " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error fetching chart data", e);
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== Statistics API ====================

    /**
     * Get trade statistics
     * GET /api/statistics/trades
     */
    public CompletableFuture<TradeStatisticsDto> getTradeStatistics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/statistics/trades"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<TradeStatisticsDto> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<TradeStatisticsDto>>() {}
                    );
                    
                    if (apiResponse.isSuccess()) {
                        logger.info("Trade statistics fetched successfully");
                        return apiResponse.getData();
                    }
                }
                throw new Exception("Failed to get trade statistics: " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error fetching trade statistics", e);
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== Trade Logs API ====================

    /**
     * Get trade logs
     * GET /api/trade-logs?limit=50
     */
    public CompletableFuture<java.util.List<TradeLogDto>> getTradeLogs(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/trade-logs?limit=" + limit))
                    .header("Authorization", "Bearer " + sessionToken)
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<java.util.List<TradeLogDto>> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<java.util.List<TradeLogDto>>>() {}
                    );
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        logger.debug("Loaded {} trade logs", apiResponse.getData().size());
                        return apiResponse.getData();
                    }
                }
                return new java.util.ArrayList<>();
                
            } catch (Exception e) {
                logger.error("Error fetching trade logs", e);
                return new java.util.ArrayList<>();
            }
        });
    }

    // ==================== Questionnaire API ====================

    /**
     * Get questionnaire questions
     * GET /api/questionnaire/questions
     */
    public CompletableFuture<QuestionnaireQuestionsDto> getQuestionnaireQuestions() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/questionnaire/questions"))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    ApiResponse<QuestionnaireQuestionsDto> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<QuestionnaireQuestionsDto>>() {}
                    );
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        logger.info("Questions loaded successfully");
                        return apiResponse.getData();
                    }
                }
                throw new Exception("Failed to load questions: HTTP " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error loading questionnaire questions", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Submit questionnaire
     * POST /api/questionnaire/submit
     */
    public CompletableFuture<QuestionnaireResultDto> submitQuestionnaire(Map<String, Integer> answers) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QuestionnaireSubmitDto submitDto = new QuestionnaireSubmitDto(answers);
                String requestBody = objectMapper.writeValueAsString(submitDto);
                
                logger.info("Submitting questionnaire with {} answers", answers.size());
                logger.debug("Request body: {}", requestBody);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/questionnaire/submit"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                logger.info("Response status: {}", response.statusCode());
                logger.debug("Response body: {}", response.body());
                
                if (response.statusCode() == 200) {
                    ApiResponse<QuestionnaireResultDto> apiResponse = objectMapper.readValue(
                        response.body(),
                        new TypeReference<ApiResponse<QuestionnaireResultDto>>() {}
                    );
                    
                    logger.debug("ApiResponse parsed - success: {}, data: {}", 
                        apiResponse.isSuccess(), 
                        apiResponse.getData() != null ? "present" : "null");
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        logger.info("Questionnaire submitted successfully");
                        return apiResponse.getData();
                    } else {
                        String errorMsg = apiResponse.getErrorMessage();
                        logger.warn("Questionnaire submission failed: {}", errorMsg);
                        logger.warn("ApiResponse: success={}, message={}, data={}", 
                            apiResponse.isSuccess(), 
                            apiResponse.getMessage(),
                            apiResponse.getData());
                        throw new Exception(errorMsg != null ? errorMsg : "설문조사 제출 실패");
                    }
                }
                throw new Exception("Failed to submit questionnaire: HTTP " + response.statusCode());
                
            } catch (Exception e) {
                logger.error("Error submitting questionnaire", e);
                throw new RuntimeException(e);
            }
        });
    }

    // ==================== Utility Methods ====================

    /**
     * Send trade log to server (existing method)
     */
    public CompletableFuture<Boolean> sendTradeLog(
            TradeDecision decision, 
            MarketAnalysis analysis,
            Double executedPrice,
            Double executedQty) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> logData = new HashMap<>();
                logData.put("symbol", "BTCUSDT");
                logData.put("decision", decision.getAction().toString());
                logData.put("confidence", decision.getConfidence());
                logData.put("briefReason", decision.getBriefReason());
                logData.put("fullReason", decision.getFullReason());
                logData.put("executedPrice", executedPrice);
                logData.put("executedQty", executedQty);
                logData.put("agentName", "MasterCoordinatorAgent");
                logData.put("createdAt", LocalDateTime.now().toString());
                
                // Market snapshot
                Map<String, Object> snapshot = new HashMap<>();
                snapshot.put("price", analysis.getCurrentPrice());
                snapshot.put("rsi", analysis.getRsi14());
                snapshot.put("macd", analysis.getMacd().value());
                snapshot.put("trend", analysis.getTrendDirection());
                logData.put("marketSnapshot", snapshot);
                
                String requestBody = objectMapper.writeValueAsString(logData);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/trade-logs"))
                    .header("Authorization", "Bearer " + sessionToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    logger.info("Trade log sent successfully");
                    return true;
                } else {
                    logger.error("Failed to send trade log: {}", response.statusCode());
                    return false;
                }
                
            } catch (Exception e) {
                logger.error("Error sending trade log", e);
                return false;
            }
        });
    }

    /**
     * Check server health
     * GET /api/health
     */
    public CompletableFuture<Boolean> checkHealth() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/health"))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                return response.statusCode() == 200;
                
            } catch (Exception e) {
                logger.error("Health check failed", e);
                return false;
            }
        });
    }

    // ==================== Session Management ====================

    /**
     * Set session token (for pre-authenticated sessions)
     */
    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    /**
     * Get current session token
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Check if authenticated
     */
    public boolean isAuthenticated() {
        return sessionToken != null && !sessionToken.isEmpty();
    }

    /**
     * Logout (clear session locally)
     */
    public void logout() {
        sessionToken = null;
        logger.info("Logged out locally");
    }
}
