package cn.gdeiassistant.common.tools.Utils;

import cn.gdeiassistant.common.pojo.Encryption.AESEncryptConfig;
import cn.gdeiassistant.common.pojo.Encryption.EncryptConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class StringEncryptUtils {

    private static final String FORMAT_VERSION = "v1:";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static EncryptConfig encryptConfig;

    @Autowired(required = false)
    public void setEncryptConfig(@Qualifier("encryptConfig") EncryptConfig encryptConfig) {
        StringEncryptUtils.encryptConfig = encryptConfig;
    }

    /**
     * 加密字符串信息，返回加密后的字符串
     *
     * @param data
     * @return
     * @throws GeneralSecurityException when encryption cannot be completed
     */
    public static String encryptString(String data) throws GeneralSecurityException {
        if (!(StringEncryptUtils.encryptConfig instanceof AESEncryptConfig aesConfig)) {
            return data;
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyFor(aesConfig), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        cipher.updateAAD(FORMAT_VERSION.getBytes(StandardCharsets.US_ASCII));
        byte[] ciphertext = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        payload.writeBytes(iv);
        payload.writeBytes(ciphertext);
        return FORMAT_VERSION + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray());

    }

    /**
     * 解密字符串信息，返回解密后的字符串
     *
     * @param data
     * @return
     * @throws GeneralSecurityException when decryption cannot be completed
     */
    public static String decryptString(String data) throws GeneralSecurityException {
        if (!(StringEncryptUtils.encryptConfig instanceof AESEncryptConfig aesConfig)) {
            return data;
        }

        if (data == null || !data.startsWith(FORMAT_VERSION)) {
            throw new GeneralSecurityException("Unsupported encrypted value format");
        }
        final byte[] payload;
        try {
            payload = Base64.getUrlDecoder().decode(data.substring(FORMAT_VERSION.length()));
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("Invalid encrypted value", e);
        }
        if (payload.length < IV_LENGTH_BYTES + TAG_LENGTH_BITS / 8) {
            throw new GeneralSecurityException("Invalid encrypted value length");
        }

        byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(payload, IV_LENGTH_BYTES, payload.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keyFor(aesConfig), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
        cipher.updateAAD(FORMAT_VERSION.getBytes(StandardCharsets.US_ASCII));
        return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
    }

    private static SecretKeySpec keyFor(AESEncryptConfig aesConfig) throws NoSuchAlgorithmException {
        String privateKey = aesConfig.getPrivateKey();
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("Encryption private key is not configured");
        }
        byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(privateKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * SHA1哈希映射字符串，返回映射后的结果
     *
     * @param text
     * @return
     */
    public static String sha1HexString(String text) {
        return DigestUtils.sha1Hex(text);
    }

    /**
     * SHA256哈希映射字符串，返回映射后的结果
     *
     * @param text
     * @return
     */
    public static String sha256HexString(String text) {
        return DigestUtils.sha256Hex(text);
    }

    /**
     * SHA512哈希映射字符串，返回映射后的结果
     *
     * @param text
     * @return
     */
    public static String sha512HexString(String text) {
        return DigestUtils.sha512Hex(text);
    }
}
