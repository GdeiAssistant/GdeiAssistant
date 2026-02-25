package cn.gdeiassistant.common.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 仅当配置项 news.admin.account 非空时成立，用于控制新闻爬虫定时任务 Bean 是否加载。
 */
public class NewsAdminAccountCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String account = context.getEnvironment().getProperty("news.admin.account");
        return StringUtils.hasText(account);
    }
}
