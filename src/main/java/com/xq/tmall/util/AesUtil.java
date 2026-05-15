package com.xq.tmall.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class AesUtil {

    private static final String AES = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 128; // AES key size in bits

    /**
     * 使用给定的密码和盐生成AES密钥。
     */
    public static SecretKey generateKeyFromPassword(String password, byte[] salt) throws Exception {
        // 使用PBKDF2从密码和盐中派生出密钥材料
        SecretKey secretKey = deriveKey(password, salt);
        return secretKey;
    }

    /**
     * 使用PBKDF2从密码和盐中派生出密钥材料。
     */
    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        int iterations = 65536; // 迭代次数
        int keyLength = KEY_SIZE / 8; // 密钥长度（字节）

        javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, iterations, keyLength * 8);
        javax.crypto.SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), AES);
    }

    /**
     * 对给定的字符串进行MD5加密。
     */
    private static String encryptToMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不被支持", e);
        }
    }

    /**
     * 加密给定的文本。
     */
    public static String encrypt(String plainText, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());

        // 将IV附加到密文之前，以便在解密时使用
        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    /**
     * 解密给定的文本。
     */
    public static String decrypt(String encryptedText, SecretKey secretKey, byte[] iv) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decrypted = cipher.doFinal(decoded, iv.length, decoded.length - iv.length);
        return new String(decrypted);
    }

    /**
     * 生成一个随机的初始化向量（IV）。
     */
    public static byte[] generateIv() {
        byte[] iv = new byte[16]; // For AES with CBC mode, the IV length is 16 bytes
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * 生成一个唯一的盐值。
     */
    public static byte[] generateSalt() {
        return UUID.randomUUID().toString().getBytes();
    }

    /**
     * 验证用户提供的密码是否正确。
     */
    public static boolean verifyPassword(String providedPassword, String storedEncryptedPassword, byte[] salt) throws Exception {
        SecretKey secretKey = generateKeyFromPassword(providedPassword, salt);
        byte[] decodedStored = Base64.getDecoder().decode(storedEncryptedPassword);
        byte[] iv = new byte[16];
        System.arraycopy(decodedStored, 0, iv, 0, iv.length);

        String decryptedPassword = decrypt(storedEncryptedPassword, secretKey, iv);
        return providedPassword.equals(decryptedPassword);
    }
}