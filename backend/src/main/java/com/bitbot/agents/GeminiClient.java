package com.bitbot.agents;

import com.bitbot.utils.ConfigLoader;
import com.bitbot.utils.RetryUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Google Gemini API 클라이언트
 * LLM 에이전트들이 사용하는 공통 API 호출 클래스
 */
public class GeminiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    
    public GeminiClient() {
        this.apiKey = ConfigLoader.getGeminiApiKey();
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Gemini API 호출
     * @param prompt 프롬프트 텍스트
     * @return LLM 응답 텍스트
     */
    public String callGemini(String prompt) throws IOException {
        return callGemini(prompt, 0.7, 8192);
    }
    
    /**
     * Gemini API 호출 (온도 및 토큰 제어)
     * @param prompt 프롬프트 텍스트
     * @param temperature 창의성 (0.0 ~ 1.0)
     * @param maxTokens 최대 토큰 수
     * @return LLM 응답 텍스트
     */
    public String callGemini(String prompt, double temperature, int maxTokens) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // 요청 본문 생성
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(Map.of("text", prompt)));
        requestBody.put("contents", List.of(content));
        
        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("maxOutputTokens", maxTokens);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        requestBody.put("generationConfig", generationConfig);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        // HTTP 요청 생성
        Request request = new Request.Builder()
                .url(API_URL + "?key=" + apiKey)
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();
        
        // API 호출 (재시도 로직 포함)
        try {
            String responseBody = RetryUtil.retryIfRetryable(
                () -> {
                    try {
                        Response response = httpClient.newCall(request).execute();
                        try {
                            if (!response.isSuccessful()) {
                                ResponseBody errorResponseBody = response.body();
                                String errorBody = errorResponseBody != null ? errorResponseBody.string() : "No error body";
                                if (errorResponseBody != null) {
                                    errorResponseBody.close();
                                }
                                throw new IOException("Gemini API Error: " + response.code() + " - " + errorBody);
                            }
                            
                            ResponseBody responseBodyObj = response.body();
                            if (responseBodyObj == null) {
                                throw new IOException("Gemini API 응답이 비어있습니다");
                            }
                            String body = responseBodyObj.string();
                            if (body.isEmpty()) {
                                throw new IOException("Gemini API 응답이 비어있습니다");
                            }
                            return body;
                        } finally {
                            response.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                3,  // 최대 3회 재시도
                2000  // 초기 지연 2초 (LLM API는 더 긴 지연)
            );
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            
            // 응답 구조 디버깅
            logger.debug("Gemini 원본 응답: {}", responseBody);
            
            // 응답에서 텍스트 추출 (안전하게)
            JsonNode candidates = jsonResponse.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                logger.error("Gemini API 응답에 candidates 없음: {}", responseBody);
                throw new IOException("Invalid Gemini response: no candidates");
            }
            
            JsonNode contentNode = candidates.get(0).get("content");
            if (contentNode == null) {
                logger.error("Gemini API 응답에 content 없음");
                throw new IOException("Invalid Gemini response: no content");
            }
            
            JsonNode parts = contentNode.get("parts");
            if (parts == null || parts.isEmpty()) {
                logger.error("Gemini API 응답에 parts 없음");
                throw new IOException("Invalid Gemini response: no parts");
            }
            
            JsonNode text = parts.get(0).get("text");
            if (text == null) {
                logger.error("Gemini API 응답에 text 없음");
                throw new IOException("Invalid Gemini response: no text");
            }
            
            String generatedText = text.asText();
            logger.debug("Gemini API 호출 성공 ({}ms): {} chars", responseTime, generatedText.length());
            
            return generatedText;
            
        } catch (Exception e) {
            logger.error("Gemini API 호출 실패 (재시도 후)", e);
            throw new IOException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * JSON 형식으로 응답을 요청하는 프롬프트 생성 헬퍼
     */
    public static String wrapPromptForJSON(String systemRole, String task, String inputData, String outputFormat) {
        return String.format("""
                # 역할 (Role)
                %s
                
                # 임무 (Task)
                %s
                
                # 입력 데이터 (Input Data)
                %s
                
                # 출력 형식 (Output Format)
                반드시 아래의 JSON 형식으로만 응답하세요. 추가 설명이나 코멘트는 제외하세요.
                
                ```json
                %s
                ```
                
                **중요**: JSON 코드 블록(```) 없이 순수 JSON만 출력하세요.
                """, systemRole, task, inputData, outputFormat);
    }
    
    /**
     * JSON 응답 파싱
     */
    public JsonNode parseJSONResponse(String response) throws IOException {
        // JSON 코드 블록 제거 (```json ... ```)
        String cleanedResponse = response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*$", "")
                .trim();
        
        return objectMapper.readTree(cleanedResponse);
    }
}


