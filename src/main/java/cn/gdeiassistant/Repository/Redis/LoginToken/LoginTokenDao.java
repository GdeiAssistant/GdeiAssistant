package cn.gdeiassistant.Repository.Redis.LoginToken;

import cn.gdeiassistant.Pojo.Entity.AccessToken;
import cn.gdeiassistant.Pojo.Entity.Device;
import cn.gdeiassistant.Pojo.Entity.RefreshToken;

public interface LoginTokenDao {

    public AccessToken QueryAccessToken(String signature);

    public RefreshToken QueryRefreshToken(String signature);

    public void InsertAccessToken(AccessToken token);

    public void InsertRefreshToken(RefreshToken token);

    public void DeleteAccessToken(String signature);

    public void DeleteRefreshToken(String signature);

    public Device QueryDeviceData(String signature);

    public void SaveDeviceData(String signature, Device device);
}
