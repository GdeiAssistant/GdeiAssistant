package cn.gdeiassistant.common.pojo.config;

import cn.gdeiassistant.common.enums.module.ModuleEnum;
import cn.gdeiassistant.common.tools.springutils.ModuleUtils;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Scope("singleton")
public class JWTConfig {

    private String secret;

    @Autowired
    private ModuleUtils moduleUtils;

    private static final String INSECURE_PLACEHOLDER = "YOUR_SECURE_JWT_SECRET_AT_LEAST_32_CHARS";

    @Value("${jwt.secret:}")
    public void setSecret(String secret) {
        if (StringUtils.isNotBlank(secret) && !INSECURE_PLACEHOLDER.equals(secret)) {
            this.secret = secret;
        } else {
            moduleUtils.disableModule(ModuleEnum.JWT);
        }
    }

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured. Set the JWT_SECRET environment variable with a secure value of at least 32 characters.");
        }
    }

    public String getSecret() {
        return secret;
    }
}
