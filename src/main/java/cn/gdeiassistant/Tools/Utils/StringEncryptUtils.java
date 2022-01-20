package cn.gdeiassistant.Tools.Utils;

import cn.gdeiassistant.Pojo.Entity.StringEncryptConfig;
import com.taobao.wsgsvr.EncryptWithCfg;
import com.taobao.wsgsvr.WsgException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class StringEncryptUtils {

    private static StringEncryptConfig stringEncryptConfig;

    @Autowired(required = false)
    @Qualifier("stringEncryptConfig")
    public void setStringEncryptConfig(StringEncryptConfig stringEncryptConfig) {
        StringEncryptUtils.stringEncryptConfig = stringEncryptConfig;
    }

    /**
     * 加密字符串信息,返回加密后的字符串
     *
     * @param data
     * @return
     * @throws WsgException
     */
    public static String encryptString(String data) throws WsgException {
        if (SpringProfileUtils.CheckDevelopmentEnvironment()) {
            //开发环境下不使用加密
            return data;
        }
        //生产环境下使用阿里聚安全加密
        if (stringEncryptConfig != null) {
            return new EncryptWithCfg(stringEncryptConfig.getConfigLocation())
                    .encryptString(stringEncryptConfig.getAppkey(), data);
        }
        return data;
    }

    /**
     * 解密字符串信息,返回解密后的字符串
     *
     * @param data
     * @return
     * @throws WsgException
     */
    public static String decryptString(String data) throws WsgException {
        if (SpringProfileUtils.CheckDevelopmentEnvironment()) {
            //开发环境下不使用加密
            return data;
        }
        //生产环境下使用阿里聚安全加密
        if (stringEncryptConfig != null) {
            return new EncryptWithCfg(stringEncryptConfig.getConfigLocation())
                    .decryptString(stringEncryptConfig.getAppkey(), data);
        }
        return null;
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
