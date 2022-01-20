package cn.gdeiassistant.Repository.Redis.LoginToken;

import cn.gdeiassistant.Pojo.Entity.AccessToken;
import cn.gdeiassistant.Pojo.Entity.RefreshToken;

public interface LoginTokenDao {

    public String QueryToken(String signature);

    public void InsertAccessToken(AccessToken token);

    public void InsertRefreshToken(RefreshToken token);

    public void UpdateAccessToken(AccessToken token);

    public void DeleteToken(String signature);
}
