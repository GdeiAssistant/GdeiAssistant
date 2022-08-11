package cn.gdeiassistant.Config.Resources;

import cn.gdeiassistant.Constant.ResourcesConstantUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ResourcesConfig {

    /**
     * 校园新闻通知系统地址列表
     *
     * @return
     */
    @Bean
    public List<List<String>> newsUrlsList() {
        return Arrays.stream(ResourcesConstantUtils.NEWS_URL_LIST)
                .map(Arrays::asList).collect(Collectors.toList());
    }

    /**
     * RSS订阅的校园新闻通知系统地址列表，新闻类型都属于综合信息
     *
     * @return
     */
    @Bean
    public List<String> rssNewsUrlsList() {
        return Arrays.asList(ResourcesConstantUtils.RSS_NEWS_URL_LIST);
    }
}
