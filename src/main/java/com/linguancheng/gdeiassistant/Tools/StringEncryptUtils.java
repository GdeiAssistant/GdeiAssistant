package com.linguancheng.gdeiassistant.Tools;

import com.taobao.wsgsvr.EncryptWithCfg;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by linguancheng on 2017/7/17.
 */

@Component
public class StringEncryptUtils {

    private static String appkey;

    private static String configLocation;

    @Value("#{propertiesReader['encryptor.appkey']}")
    public void setAppkey(String appkey) {
        StringEncryptUtils.appkey = appkey;
    }

    @Value("#{propertiesReader['encryptor.config.location']}")
    public void setConfigLocation(String configLocation) {
        StringEncryptUtils.configLocation = configLocation;
    }

    /**
     * 加密字符串信息,返回加密后的字符串
     *
     * @param data
     * @return
     * @throws WsgException
     */
    public static String encryptString(String data) throws WsgException {
        return new EncryptWithCfg(configLocation).encryptString(appkey, data);
    }

    /**
     * 解密字符串信息,返回解密后的字符串
     *
     * @param data
     * @return
     * @throws WsgException
     */
    public static String decryptString(String data) throws WsgException {
        return new EncryptWithCfg(configLocation).decryptString(appkey, data);
    }

    /**
     * SHA1映射字符串,返回映射后的结果
     *
     * @param text
     * @return
     */
    public static String SHA1MapString(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(text.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String shaHex = Integer.toHexString(aMessageDigest & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
