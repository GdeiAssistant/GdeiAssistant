package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YiBanAuthorizeInfo implements Serializable, Entity {

    private String appID;

    private String appSecret;

    private String callbackURL;

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public YiBanAuthorizeInfo() {

    }

    public YiBanAuthorizeInfo(String appID, String appSecret, String callbackURL) {
        this.appID = appID;
        this.appSecret = appSecret;
        this.callbackURL = callbackURL;
    }
}
