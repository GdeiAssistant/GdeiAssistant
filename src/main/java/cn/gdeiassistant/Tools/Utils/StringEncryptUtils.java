package cn.gdeiassistant.Tools.Utils;

import cn.gdeiassistant.Pojo.Encryption.AESEncryptConfig;
import cn.gdeiassistant.Pojo.Encryption.EncryptConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class StringEncryptUtils {

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
     * @throws NoSuchAlgorithmException
     */
    public static String encryptString(String data) throws NoSuchAlgorithmException
            , NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
            , InvalidKeyException {
        if (StringEncryptUtils.encryptConfig != null) {
            switch (StringEncryptUtils.encryptConfig.getClass().getSimpleName()) {
                case "JAQEncryptConfig":
                    //使用阿里聚安全数据算法加密
                    /*return new EncryptWithCfg(((JAQEncryptConfig) StringEncryptUtils.encryptConfig)
                            .getConfigLocation()).encryptString(((JAQEncryptConfig) StringEncryptUtils.encryptConfig)
                            .getAppKey(), data);*/
                    break;

                case "AESEncryptConfig":
                    //使用AES对称加密算法加密
                    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                    secureRandom.setSeed(((AESEncryptConfig) StringEncryptUtils.encryptConfig)
                            .getPrivateKey().getBytes(StandardCharsets.UTF_8));
                    byte[] randomBytes = ((AESEncryptConfig) StringEncryptUtils.encryptConfig)
                            .getPrivateKey().getBytes(StandardCharsets.UTF_8).clone();
                    secureRandom.nextBytes(randomBytes);
                    SecretKeySpec key = new SecretKeySpec(randomBytes, "AES");
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                    return Base64.getUrlEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
            }
        }
        //没有匹配的加密类型，返回原文
        return data;

    }

    /**
     * 解密字符串信息，返回解密后的字符串
     *
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String decryptString(String data) throws NoSuchAlgorithmException
            , NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
            , InvalidKeyException {
        if (StringEncryptUtils.encryptConfig != null) {
            switch (StringEncryptUtils.encryptConfig.getClass().getSimpleName()) {
                case "JAQEncryptConfig":
                    //使用阿里聚安全数据算法解密
                    /*return new EncryptWithCfg(((JAQEncryptConfig) StringEncryptUtils.encryptConfig)
                            .getConfigLocation()).decryptString(((JAQEncryptConfig) StringEncryptUtils.encryptConfig)
                            .getAppKey(), data);*/
                    break;

                case "AESEncryptConfig":
                    //使用AES对称加密算法解密
                    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                    secureRandom.setSeed(((AESEncryptConfig) StringEncryptUtils.encryptConfig)
                            .getPrivateKey().getBytes(StandardCharsets.UTF_8));
                    byte[] randomBytes = ((AESEncryptConfig) StringEncryptUtils.encryptConfig)
                            .getPrivateKey().getBytes(StandardCharsets.UTF_8).clone();
                    secureRandom.nextBytes(randomBytes);
                    SecretKeySpec key = new SecretKeySpec(randomBytes, "AES");
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.DECRYPT_MODE, key);
                    return new String(cipher.doFinal(Base64.getUrlDecoder().decode(data)), StandardCharsets.UTF_8);
            }
        }
        //没有匹配的加密类型，返回原文
        return data;
    }

    /**
     * SHA1哈希映射字符串，返回映射后的结果
     *
     * @param text
     * @return
     */
    public static String SHA1HexString(String text) {
        return DigestUtils.sha1Hex(text);
    }

    /**
     * SHA256哈希映射字符串，返回映射后的结果
     *
     * @param text
     * @return
     */
    public static String SHA256HexString(String text) {
        return DigestUtils.sha256Hex(text);
    }

    /**
     * SHA512哈希映射字符串，返回映射后的结果
     *
     * @param text
     * @return
     */
    public static String SHA512HexString(String text) {
        return DigestUtils.sha512Hex(text);
    }
}
