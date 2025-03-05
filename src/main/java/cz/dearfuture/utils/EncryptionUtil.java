package cz.dearfuture.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Simple utility class for basic string encryption.
 */
public class EncryptionUtil {
    private static final String KEY = "MySecretKey69960"; // 16 characters for AES
    private static final String ALGO = "AES";

    /**
     * Encrypts a string using AES encryption.
     */
    public static String encrypt(String value) {
        if (value == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGO);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.out.println(e);
            return value;
        }
    }

    /**
     * Decrypts an AES encrypted string.
     */
    public static String decrypt(String encrypted) {
        if (encrypted == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGO);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted);
        } catch (Exception e) {
            return encrypted;
        }
    }
}
