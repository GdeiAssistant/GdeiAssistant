package edu.gdei.gdeiassistant.Repository.Redis.LoginToken;

import edu.gdei.gdeiassistant.Pojo.Entity.AccessToken;
import edu.gdei.gdeiassistant.Pojo.Entity.RefreshToken;

public interface LoginTokenDao {

    public String QueryToken(String signature);

    public void InsertAccessToken(AccessToken token);

    public void InsertRefreshToken(RefreshToken token);

    public void UpdateAccessToken(AccessToken token);

    public Boolean DeleteToken(String signature);
}
