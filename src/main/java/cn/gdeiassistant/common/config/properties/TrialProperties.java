package cn.gdeiassistant.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 与 trial.* 配置绑定的样板间配置类。
 * 目前仅维护测试账号白名单，后续可扩展模块级开关等。
 */
@Component
@ConfigurationProperties(prefix = "trial")
public class TrialProperties {

    /**
     * 测试账号白名单列表，对应 trial.test-accounts 配置。
     */
    private List<String> testAccounts = new ArrayList<>();

    public List<String> getTestAccounts() {
        return testAccounts;
    }

    public void setTestAccounts(List<String> testAccounts) {
        this.testAccounts = testAccounts;
    }
}

