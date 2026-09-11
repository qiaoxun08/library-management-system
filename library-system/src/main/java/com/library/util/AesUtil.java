package com.library.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 加密工具类
 *
 * 用于敏感字段加密存储（手机号、身份证号等）
 * 使用 AES/CBC/PKCS5Padding + 随机 IV（IV 与密文拼接后 Base64 编码）
 * 相同明文每次产生不同密文，防止密文模式泄露
 */
public class AesUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final int KEY_LENGTH = 32; // AES-256

    /**
     * 加密
     * @param plaintext 明文
     * @return Base64(IV + 密文)
     */
    public static String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        try {
            SecretKey key = getSecretKey();
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            // IV 拼接在密文前面，解密时先取前16字节作为 IV
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    /**
     * 解密
     * @param ciphertext Base64(IV + 密文)
     * @return 明文
     */
    public static String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        try {
            SecretKey key = getSecretKey();
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            if (combined.length <= IV_LENGTH) {
                throw new IllegalArgumentException("密文格式错误");
            }
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败", e);
        }
    }

    /**
     * 生成随机 AES 密钥
     * @param keySize 密钥长度（128/192/256）
     * @return Base64 编码的密钥
     */
    public static String generateKey(int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(keySize, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成 AES 密钥失败", e);
        }
    }

    /**
     * 从环境变量 AES_SECRET_KEY 读取密钥
     * 必须配置，缺失时抛出异常（不使用硬编码默认密钥）
     * 生成方式：openssl rand -base64 32
     */
    private static SecretKey getSecretKey() {
        String secretKey = System.getenv("AES_SECRET_KEY");
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("缺少 AES_SECRET_KEY 环境变量，请配置 AES 加密密钥");
        }
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        // 截取或填充到 32 字节（AES-256）
        byte[] validKey = new byte[KEY_LENGTH];
        System.arraycopy(keyBytes, 0, validKey, 0, Math.min(keyBytes.length, KEY_LENGTH));
        return new SecretKeySpec(validKey, ALGORITHM);
    }
}
