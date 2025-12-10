package com.bitbot.client.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Secure Credentials Storage
 * Manages encrypted API keys and sensitive credentials
 * 
 * File Format (JSON):
 * {
 *   "salt": "base64_salt",
 *   "binance_api_key": "encrypted_data",
 *   "binance_secret_key": "encrypted_data",
 *   "gemini_api_key": "encrypted_data",
 *   "server_session_token": "encrypted_data"
 * }
 * 
 * Security:
 * - All credentials are encrypted with user password
 * - Salt is stored alongside encrypted data
 * - Password is NEVER stored
 */
public class CredentialStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(CredentialStorage.class);
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.bitbot";
    private static final String CONFIG_FILE = "config.dat";
    
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;
    private final Path configPath;

    public CredentialStorage() {
        this.encryptionService = new EncryptionService();
        this.objectMapper = new ObjectMapper();
        this.configPath = Paths.get(CONFIG_DIR, CONFIG_FILE);
        
        // Ensure config directory exists
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
        } catch (IOException e) {
            logger.error("Failed to create config directory", e);
        }
    }

    /**
     * Save credentials to encrypted file
     * 
     * @param password User password for encryption
     * @param credentials Map of credential keys to values
     * @throws Exception if save fails
     */
    public void saveCredentials(String password, Map<String, String> credentials) throws Exception {
        try {
            // Generate salt
            byte[] salt = encryptionService.generateSalt();
            
            // Prepare data structure
            Map<String, String> encryptedData = new HashMap<>();
            encryptedData.put("salt", encryptionService.saltToString(salt));
            
            // Encrypt each credential
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                String encrypted = encryptionService.encrypt(entry.getValue(), password, salt);
                encryptedData.put(entry.getKey(), encrypted);
            }
            
            // Write to file
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(configPath.toFile(), encryptedData);
            
            logger.info("Credentials saved successfully to {}", configPath);
            
        } catch (Exception e) {
            logger.error("Failed to save credentials", e);
            throw new Exception("Failed to save credentials: " + e.getMessage(), e);
        }
    }

    /**
     * Load and decrypt credentials from file
     * 
     * @param password User password for decryption
     * @return Map of credential keys to decrypted values
     * @throws Exception if load fails (wrong password, corrupted file, etc.)
     */
    public Map<String, String> loadCredentials(String password) throws Exception {
        try {
            // Check if config file exists
            if (!Files.exists(configPath)) {
                throw new Exception("No credentials found. Please configure API keys first.");
            }
            
            // Read from file
            @SuppressWarnings("unchecked")
            Map<String, String> encryptedData = objectMapper.readValue(
                configPath.toFile(),
                Map.class
            );
            
            // Extract salt
            String saltString = encryptedData.get("salt");
            if (saltString == null) {
                throw new Exception("Invalid config file: salt not found");
            }
            byte[] salt = encryptionService.saltFromString(saltString);
            
            // Decrypt each credential
            Map<String, String> credentials = new HashMap<>();
            for (Map.Entry<String, String> entry : encryptedData.entrySet()) {
                if (entry.getKey().equals("salt")) {
                    continue; // Skip salt
                }
                
                try {
                    String decrypted = encryptionService.decrypt(entry.getValue(), password, salt);
                    credentials.put(entry.getKey(), decrypted);
                } catch (Exception e) {
                    logger.error("Failed to decrypt: {}", entry.getKey());
                    throw new Exception("Wrong password or corrupted data");
                }
            }
            
            logger.info("Credentials loaded successfully");
            return credentials;
            
        } catch (IOException e) {
            logger.error("Failed to read credentials file", e);
            throw new Exception("Failed to load credentials: " + e.getMessage(), e);
        }
    }

    /**
     * Check if credentials file exists
     */
    public boolean credentialsExist() {
        return Files.exists(configPath);
    }

    /**
     * Delete credentials file
     */
    public void deleteCredentials() throws IOException {
        if (Files.exists(configPath)) {
            Files.delete(configPath);
            logger.info("Credentials deleted");
        }
    }

    /**
     * Update a single credential
     * 
     * @param password User password
     * @param key Credential key
     * @param value New credential value
     * @throws Exception if update fails
     */
    public void updateCredential(String password, String key, String value) throws Exception {
        Map<String, String> credentials = credentialsExist() 
            ? loadCredentials(password) 
            : new HashMap<>();
        
        credentials.put(key, value);
        saveCredentials(password, credentials);
    }

    /**
     * Get a single credential
     * 
     * @param password User password
     * @param key Credential key
     * @return Decrypted credential value
     * @throws Exception if credential not found or decryption fails
     */
    public String getCredential(String password, String key) throws Exception {
        Map<String, String> credentials = loadCredentials(password);
        String value = credentials.get(key);
        
        if (value == null) {
            throw new Exception("Credential not found: " + key);
        }
        
        return value;
    }

    /**
     * Get config file path
     */
    public String getConfigPath() {
        return configPath.toString();
    }
}


