package cn.gdeiassistant.core.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Component
public class DemoAccount {

    private final boolean enabled;
    private final String username;
    private final byte[] password;

    public DemoAccount(Environment environment,
                       @Value("${app.demo.account-enabled:false}") boolean enabled,
                       @Value("${app.demo.username:}") String username,
                       @Value("${app.demo.password:}") String password) {
        this.enabled = enabled && !username.isBlank() && !password.isBlank()
                && Arrays.stream(environment.getActiveProfiles()).noneMatch("production"::equalsIgnoreCase);
        this.username = username;
        this.password = password.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean matches(String username) {
        return enabled && this.username.equals(username);
    }

    public boolean passwordMatches(String candidate) {
        return candidate != null && MessageDigest.isEqual(password, candidate.getBytes(StandardCharsets.UTF_8));
    }
}
