package com.bitbot.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Common API Response wrapper
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("data")
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
    
    public String getErrorMessage() {
        if (error != null) return error;
        if (message != null) return message;
        return "Unknown error occurred";
    }

    @Override
    public String toString() {
        return String.format("ApiResponse{success=%s, message='%s', data=%s}", 
            success, message, data);
    }
}


