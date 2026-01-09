package com.the_pathfinders.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Properties;

/**
 * Utility for encrypting and decrypting configuration files containing sensitive data.
 * Uses AES-256 encryption with PBKDF2 key derivation.
 */
public class EncryptedConfig {
    
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 10000; // Reduced from 65536 for faster startup (still secure)
    
    // Master password components (obfuscated - split across multiple parts)
    // In production, consider using Java KeyStore or HSM
    private static final String PART1 = "Sh3lt3r";
    private static final String PART2 = "F0r";
    private static final String PART3 = "M1nd";
    private static final String PART4 = "S3cur3";
    private static final String PART5 = "2026";
    
    /**
     * Get the master password (reconstructed from obfuscated parts)
     */
    private static String getMasterPassword() {
        // Additional obfuscation: read from optional keyfile if exists
        String keyfilePath = "config/.keyfile";
        File keyfile = new File(keyfilePath);
        if (keyfile.exists()) {
            try {
                return new String(Files.readAllBytes(Paths.get(keyfilePath)), StandardCharsets.UTF_8).trim();
            } catch (IOException e) {
                System.err.println("Warning: Could not read keyfile, using default");
            }
        }
        // Fallback: use obfuscated components
        return PART1 + PART2 + PART3 + PART4 + PART5;
    }
    
    /**
     * Derive encryption key from password and salt using PBKDF2
     */
    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }
    
    /**
     * Encrypt a properties file and save to encrypted format
     */
    public static void encryptConfig(String plainPropertiesPath, String encryptedOutputPath) throws Exception {
        // Load plain properties
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(plainPropertiesPath)) {
            props.load(fis);
        }
        
        // Convert to string
        StringWriter writer = new StringWriter();
        props.store(writer, "Encrypted Database Configuration");
        String plaintext = writer.toString();
        
        // Generate random salt and IV
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);
        
        // Derive key and encrypt
        SecretKey key = deriveKey(getMasterPassword(), salt);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // Write salt + iv + encrypted data
        try (FileOutputStream fos = new FileOutputStream(encryptedOutputPath)) {
            fos.write(salt);
            fos.write(iv);
            fos.write(encrypted);
        }
        
        System.out.println("✓ Configuration encrypted successfully to: " + encryptedOutputPath);
    }
    
    /**
     * Decrypt and load properties from encrypted file
     */
    public static Properties decryptConfig(String encryptedPath) throws Exception {
        // Read encrypted file
        byte[] fileContent = Files.readAllBytes(Paths.get(encryptedPath));
        
        // Extract salt, iv, and encrypted data
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[fileContent.length - 32];
        
        System.arraycopy(fileContent, 0, salt, 0, 16);
        System.arraycopy(fileContent, 16, iv, 0, 16);
        System.arraycopy(fileContent, 32, encrypted, 0, encrypted.length);
        
        // Derive key and decrypt
        SecretKey key = deriveKey(getMasterPassword(), salt);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);
        
        // Parse properties
        Properties props = new Properties();
        try (StringReader reader = new StringReader(new String(decrypted, StandardCharsets.UTF_8))) {
            props.load(reader);
        }
        
        return props;
    }
    
    /**
     * Load database configuration from encrypted config file
     * Falls back to db.properties if encrypted file doesn't exist
     */
    public static Properties loadDatabaseConfig() {
        System.out.println("Loading database configuration...");
        long startTime = System.currentTimeMillis();
        
        // Try 1: Load from classpath (works in packaged apps)
        try (InputStream is = EncryptedConfig.class.getClassLoader().getResourceAsStream("config/db.enc")) {
            if (is != null) {
                System.out.println("Found encrypted config in classpath");
                byte[] fileContent = is.readAllBytes();
                
                // Extract salt, iv, and encrypted data
                byte[] salt = new byte[16];
                byte[] iv = new byte[16];
                byte[] encrypted = new byte[fileContent.length - 32];
                
                System.arraycopy(fileContent, 0, salt, 0, 16);
                System.arraycopy(fileContent, 16, iv, 0, 16);
                System.arraycopy(fileContent, 32, encrypted, 0, encrypted.length);
                
                // Derive key and decrypt
                SecretKey key = deriveKey(getMasterPassword(), salt);
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                byte[] decrypted = cipher.doFinal(encrypted);
                
                // Parse properties
                Properties props = new Properties();
                try (StringReader reader = new StringReader(new String(decrypted, StandardCharsets.UTF_8))) {
                    props.load(reader);
                }
                
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("✓ Encrypted config loaded from classpath in " + elapsed + "ms");
                return props;
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load encrypted config from classpath: " + e.getMessage());
        }
        
        // Try 2: Load from file system (for development)
        String[] encryptedPaths = {
            "config/db.enc",                           // Development: relative to project root
            "../config/db.enc",                        // Packaged: relative to app/libs
            System.getProperty("user.dir") + "/config/db.enc"  // Absolute from working dir
        };
        
        for (String encryptedPath : encryptedPaths) {
            File encFile = new File(encryptedPath);
            if (encFile.exists()) {
                System.out.println("Found encrypted config at: " + encryptedPath);
                try {
                    Properties props = decryptConfig(encryptedPath);
                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.println("✓ Encrypted config loaded in " + elapsed + "ms");
                    return props;
                } catch (Exception e) {
                    System.err.println("Warning: Could not decrypt config from " + encryptedPath + ": " + e.getMessage());
                }
            }
        }
        
        System.err.println("Warning: Encrypted config not found in classpath or file system");
        System.err.println("Falling back to plain properties file from classpath");
        
        // Fallback to plain properties from classpath
        Properties props = new Properties();
        try (InputStream is = EncryptedConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                throw new FileNotFoundException("db.properties not found in classpath");
            }
        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            throw new RuntimeException("Failed to load database configuration", e);
        }
        
        return props;
    }
    
    /**
     * Main method for encrypting configuration files
     * Usage: java EncryptedConfig <plain-properties-file> <output-encrypted-file>
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java EncryptedConfig <input-plain-properties> <output-encrypted-file>");
            System.out.println("Example: java EncryptedConfig db.properties config/db.enc");
            return;
        }
        
        try {
            // Create config directory if it doesn't exist
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdir();
            }
            
            encryptConfig(args[0], args[1]);
            System.out.println("\n✓ Encryption complete!");
            System.out.println("  You can now safely delete the plain properties file.");
            System.out.println("  Keep the encrypted file secure and ensure proper file permissions.");
        } catch (Exception e) {
            System.err.println("✗ Encryption failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
