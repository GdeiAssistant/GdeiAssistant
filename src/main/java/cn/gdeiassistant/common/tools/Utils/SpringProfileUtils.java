package cn.gdeiassistant.common.tools.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringProfileUtils {

    private static Environment environment;

    private static boolean hasProfile(String targetProfile) {
        if (environment == null) {
            return false;
        }
        for (String profile : environment.getActiveProfiles()) {
            if (targetProfile.equals(profile)) {
                return true;
            }
        }
        for (String profile : environment.getDefaultProfiles()) {
            if (targetProfile.equals(profile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前的Spring Profile环境名称
     *
     * @return
     */
    public static String getSpringProfileName() {
        if (environment.getActiveProfiles().length == 0) {
            return environment.getDefaultProfiles()[0];
        }
        return environment.getActiveProfiles()[0];
    }

    /**
     * 检测是否为开发环境
     *
     * @return
     */
    public static boolean checkDevelopmentEnvironment() {
        return hasProfile("development");
    }

    /**
     * 检测是否为测试环境
     *
     * @return
     */
    public static boolean checkStagingEnvironment() {
        return hasProfile("staging");
    }

    /**
     * 检测是否为生产环境
     *
     * @return
     */
    public static boolean checkProductionEnvironment() {
        return hasProfile("production");
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        SpringProfileUtils.environment = environment;
    }
}
