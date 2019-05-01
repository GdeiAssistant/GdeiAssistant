package edu.gdei.gdeiassistant.Tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringProfileUtils {

    private static Environment environment;

    /**
     * 检测是否为开发环境
     *
     * @return
     */
    public static boolean CheckDevelopmentEnvironment() {
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equals("development")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测是否为生产环境
     *
     * @return
     */
    public static boolean CheckProductionEnvironment() {
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equals("production")) {
                return true;
            }
        }
        return false;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        SpringProfileUtils.environment = environment;
    }
}
