package edu.gdei.gdeiassistant.Pojo.YiBan;

import com.google.gson.annotations.SerializedName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class YiBanTokenJsonResult {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("userid")
    private String userID;

    @SerializedName("expires")
    private String expires;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }
}
