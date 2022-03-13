package cn.gdeiassistant.Pojo.Encryption;

public class JAQEncryptConfig extends EncryptConfig {

    //加密使用的appKey
    private String appKey;

    //配置文件路径
    private String configLocation;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }
}
