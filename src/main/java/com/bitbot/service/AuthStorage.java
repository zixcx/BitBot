package com.bitbot.service;

import java.util.prefs.Preferences;

public class AuthStorage {

    private static final Preferences prefs = Preferences.userRoot().node("BitBotAuth");

    // ✅ 저장
    public static void saveToken(String accessToken, String refreshToken, String email) {
        prefs.put("access_token", accessToken);
        prefs.put("refresh_token", refreshToken);
        prefs.put("email", email);
    }

    // ✅ 읽기
    public static String getAccessToken() {
        return prefs.get("access_token", null);
    }

    public static String getRefreshToken() {
        return prefs.get("refresh_token", null);
    }

    public static String getEmail() {
        return prefs.get("email", null);
    }

    // ✅ 로그아웃 시 삭제
    public static void clear() {
        prefs.remove("access_token");
        prefs.remove("refresh_token");
        prefs.remove("email");
    }
}