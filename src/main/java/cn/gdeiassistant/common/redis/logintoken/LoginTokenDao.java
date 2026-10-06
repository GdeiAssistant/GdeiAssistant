package cn.gdeiassistant.common.redis.logintoken;

import cn.gdeiassistant.common.pojo.entity.AccessToken;
import cn.gdeiassistant.common.pojo.entity.Device;
import cn.gdeiassistant.common.pojo.entity.RefreshToken;

public interface LoginTokenDao {

    AccessToken queryAccessToken(String signature);

    RefreshToken queryRefreshToken(String signature);

    void insertAccessToken(AccessToken token);

    void insertRefreshToken(RefreshToken token);

    void deleteAccessToken(String signature);

    void deleteRefreshToken(String signature);

    Device queryDeviceData(String signature);

    void saveDeviceData(String signature, Device device);
}
