package cn.gdeiassistant.common.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解。基于客户端 IP 的滑动窗口限流。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 时间窗口内允许的最大请求数
     */
    int maxRequests() default 10;

    /**
     * 时间窗口（秒）
     */
    int windowSeconds() default 60;
}
