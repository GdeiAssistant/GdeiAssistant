package cn.gdeiassistant.Tools.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringProfileUtils {

    private static Environment environment;

    /**
     * 获取当前的Spring Profile环境名称
     *
     * @return
     */
    public static String GetSpringProfileName() {
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
    public static boolean CheckDevelopmentEnvironment() {
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equals("development")) {
                return true;
            }
            if (profile.equals("production")) {
                return false;
            }
        }
        for (String profile : environment.getDefaultProfiles()) {
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
            if (profile.equals("development")) {
                return false;
            }
        }
        for (String profile : environment.getDefaultProfiles()) {
            if (profile.equals("development")) {
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
