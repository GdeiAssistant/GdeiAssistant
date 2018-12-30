package com.linguancheng.gdeiassistant.Tools;

import com.taobao.wsgsvr.EncryptWithCfg;
import com.taobao.wsgsvr.WsgException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
}
