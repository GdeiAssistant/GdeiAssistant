package cn.gdeiassistant.common.redis.LoginToken;

import cn.gdeiassistant.common.pojo.Entity.AccessToken;
import cn.gdeiassistant.common.pojo.Entity.Device;
import cn.gdeiassistant.common.pojo.Entity.RefreshToken;

public interface LoginTokenDao {

    AccessToken QueryAccessToken(String signature);

    RefreshToken QueryRefreshToken(String signature);

    void InsertAccessToken(AccessToken token);

    void InsertRefreshToken(RefreshToken token);

    void DeleteAccessToken(String signature);

    void DeleteRefreshToken(String signature);

    Device QueryDeviceData(String signature);

    void SaveDeviceData(String signature, Device device);

    // --------------- Web 登录（可撤销 Stateful JWT）：JWT 内为 token，Redis 存 token -> sessionId ---------------
    void InsertWebLoginToken(String token, String sessionId);

    String QuerySessionIdByWebToken(String token);

    String QueryWebTokenBySessionId(String sessionId);

    void DeleteWebLoginToken(String token);

    void DeleteWebLoginTokenBySessionId(String sessionId);
}
