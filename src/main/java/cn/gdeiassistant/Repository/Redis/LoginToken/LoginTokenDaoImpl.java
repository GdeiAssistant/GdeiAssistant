package cn.gdeiassistant.Repository.Redis.LoginToken;

import cn.gdeiassistant.Pojo.Entity.AccessToken;
import cn.gdeiassistant.Pojo.Entity.Device;
import cn.gdeiassistant.Pojo.Entity.RefreshToken;
import cn.gdeiassistant.Tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Repository
public class LoginTokenDaoImpl implements LoginTokenDao {

    private final String ACCESS_TOKEN_PREFIX = "ACCESS_TOKEN_";

    private final String REFRESH_TOKEN_PREFIX = "REFRESH_TOKEN_";

    private final String DEVICE_DATA_PREFIX = "DEVICE_DATA_";

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    /**
     * 查找令牌签名对应的记录信息
     *
     * @param signature
     * @return
     */
    @Override
    public AccessToken QueryAccessToken(String signature) {
        return redisDaoUtils.get(StringEncryptUtils.SHA256HexString(ACCESS_TOKEN_PREFIX + signature));
    }

    /**
     * 查找令牌签名对应的记录信息
     *
     * @param signature
     * @return
     */
    @Override
    public RefreshToken QueryRefreshToken(String signature) {
        return redisDaoUtils.get(StringEncryptUtils.SHA256HexString(REFRESH_TOKEN_PREFIX + signature));
    }

    /**
     * 插入权限令牌信息
     *
     * @param token
     */
    @Override
    public void InsertAccessToken(AccessToken token) {
        //保存权限令牌，设置有效期为7天
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(ACCESS_TOKEN_PREFIX + token.getSignature()), token);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(ACCESS_TOKEN_PREFIX + token.getSignature())
                , 7, TimeUnit.DAYS);
    }

    /**
     * 插入刷新令牌信息
     *
     * @param token
     */
    @Override
    public void InsertRefreshToken(RefreshToken token) {
        //保存刷新令牌，设置有效期为30天
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(REFRESH_TOKEN_PREFIX + token.getSignature())
                , token.getAccessTokenSignature());
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(REFRESH_TOKEN_PREFIX + token.getSignature())
                , 30, TimeUnit.DAYS);
    }

    /**
     * 删除权限令牌
     *
     * @param signature
     * @return
     */
    @Override
    public void DeleteAccessToken(String signature) {
        redisDaoUtils.delete(StringEncryptUtils.SHA256HexString(ACCESS_TOKEN_PREFIX + signature));
    }

    /**
     * 删除刷新令牌
     *
     * @param signature
     * @return
     */
    @Override
    public void DeleteRefreshToken(String signature) {
        redisDaoUtils.delete(StringEncryptUtils.SHA256HexString(REFRESH_TOKEN_PREFIX + signature));
    }

    /**
     * 查询访问设备信息
     *
     * @param signature
     * @return
     */
    @Override
    public Device QueryDeviceData(String signature) {
        return redisDaoUtils.get(StringEncryptUtils.SHA256HexString(DEVICE_DATA_PREFIX + signature));
    }

    /**
     * 保存访问设备信息
     *
     * @param signature
     * @param device
     */
    @Override
    public void SaveDeviceData(String signature, Device device) {
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(DEVICE_DATA_PREFIX + signature), (Serializable) device);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(DEVICE_DATA_PREFIX + signature), 7, TimeUnit.DAYS);
    }

}
