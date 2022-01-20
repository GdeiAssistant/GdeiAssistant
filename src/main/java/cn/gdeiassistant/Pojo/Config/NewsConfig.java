package cn.gdeiassistant.Pojo.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class NewsConfig {

    private String adminAccount;

    private String adminPassword;

    @Value("#{propertiesReader['news.admin.account']}")
    public void setAdminAccount(String adminAccount) {
        this.adminAccount = adminAccount;
    }

    @Value("#{propertiesReader['news.admin.password']}")
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}
