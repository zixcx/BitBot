package com.bitbot.client.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * AES-256-GCM Encryption Service
 * Provides secure encryption/decryption for API keys and sensitive data
 * 
 * Key Features:
 * - AES-256-GCM mode (authenticated encryption)
 * - PBKDF2 key derivation from user password
 * - 128-bit authentication tag
 * - Random IV (Initialization Vector) for each encryption
 * 
 * Security Note:
 * - Keys are NEVER stored or transmitted
 * - User password is NEVER stored
 * - Only encrypted data is persisted to disk
 */
public class EncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    
    // AES-256-GCM Constants
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    // PBKDF2 Constants
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 100000;
    private static final int SALT_LENGTH = 32;
    
    private final SecureRandom secureRandom;

    public EncryptionService() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Encrypt plaintext using password-derived key
     * 
     * @param plaintext Data to encrypt
     * @param password User password
     * @param salt Salt for key derivation (should be stored alongside encrypted data)
     * @return Base64-encoded encrypted data (IV + ciphertext + tag)
     * @throws Exception if encryption fails
     */
    public String encrypt(String plaintext, String password, byte[] salt) throws Exception {
        try {
            // Derive encryption key from password
            SecretKey key = deriveKey(password, salt);
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            
            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            // Return as Base64
            String encrypted = Base64.getEncoder().encodeToString(byteBuffer.array());
            logger.debug("Encrypted data (length: {})", encrypted.length());
            return encrypted;
            
        } catch (Exception e) {
            logger.error("Encryption failed", e);
            throw new Exception("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt ciphertext using password-derived key
     * 
     * @param encryptedData Base64-encoded encrypted data
     * @param password User password
     * @param salt Salt used during encryption
     * @return Decrypted plaintext
     * @throws Exception if decryption fails (wrong password, corrupted data, etc.)
     */
    public String decrypt(String encryptedData, String password, byte[] salt) throws Exception {
        try {
            // Derive decryption key from password
            SecretKey key = deriveKey(password, salt);
            
            // Decode Base64
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            // Decrypt
            byte[] plaintext = cipher.doFinal(ciphertext);
            
            String decrypted = new String(plaintext, StandardCharsets.UTF_8);
            logger.debug("Decrypted data successfully");
            return decrypted;
            
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Derive encryption key from password using PBKDF2
     * 
     * @param password User password
     * @param salt Random salt
     * @return Derived SecretKey
     */
    private SecretKey deriveKey(String password, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        KeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_SIZE
        );
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    /**
     * Generate a random salt for key derivation
     * 
     * @return Random salt bytes
     */
    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        logger.debug("Generated salt (length: {})", salt.length);
        return salt;
    }

    /**
     * Convert salt to Base64 string for storage
     */
    public String saltToString(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Convert Base64 string back to salt bytes
     */
    public byte[] saltFromString(String saltString) {
        return Base64.getDecoder().decode(saltString);
    }

    /**
     * Validate password strength
     * 
     * @param password Password to validate
     * @return true if password meets security requirements
     */
    public boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one uppercase, one lowercase, one digit
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpper && hasLower && hasDigit;
    }
}


