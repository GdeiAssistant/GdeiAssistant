package cn.gdeiassistant.common.pojo.Encryption;

/**
 * 加密配置 POJO，由 StringEncryptionConfig 中的 @Bean 方法条件化创建，勿加 @Component 等注解。
 */
public class EncryptConfig {

    //是否启用加密
    private boolean enable;

    //加密类型
    private String type;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
