package cn.gdeiassistant.Repository.Redis.LoginToken;

import cn.gdeiassistant.Pojo.Entity.AccessToken;
import cn.gdeiassistant.Pojo.Entity.Device;
import cn.gdeiassistant.Pojo.Entity.RefreshToken;

public interface LoginTokenDao {

    AccessToken QueryAccessToken(String signature);

    RefreshToken QueryRefreshToken(String signature);

    void InsertAccessToken(AccessToken token);

    void InsertRefreshToken(RefreshToken token);

    void DeleteAccessToken(String signature);

    void DeleteRefreshToken(String signature);

    Device QueryDeviceData(String signature);

    void SaveDeviceData(String signature, Device device);
}
