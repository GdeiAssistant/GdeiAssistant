package edu.gdei.gdeiassistant.Tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WelcomeUtils {

    private static String welcomeTitle;

    private static String welcomeContent;

    @Value("#{propertiesReader['welcome.content']}")
    public void setWelcomeContent(String welcomeContent) {
        WelcomeUtils.welcomeContent = welcomeContent;
    }

    @Value("#{propertiesReader['welcome.title']}")
    public void setWelcomeTitle(String welcomeTitle) {
        WelcomeUtils.welcomeTitle = welcomeTitle;
    }

    /**
     * 获取主页欢迎标题
     *
     * @return
     */
    public static String getWelcomeTitle() {
        return WelcomeUtils.welcomeTitle;
    }

    /**
     * 获取主页欢迎内容
     *
     * @return
     */
    public static String getWelcomeContent() {
        return WelcomeUtils.welcomeContent;
    }
}
