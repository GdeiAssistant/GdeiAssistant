package cn.gdeiassistant.common.config;

import cn.gdeiassistant.common.aspect.CronVerificationAspect;
import cn.gdeiassistant.common.aspect.IPAddressAspect;
import cn.gdeiassistant.common.aspect.LoginTokenAspect;
import cn.gdeiassistant.common.aspect.QueryLogAspect;
import cn.gdeiassistant.common.aspect.RequestLogAspect;
import cn.gdeiassistant.common.aspect.TrialDataAspect;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * GraalVM Native Image 运行时 Hint 注册器。
 * 为使用 Spring AOP 的切面类显式注册反射访问，避免 Native 模式下被裁剪。
 */
public class GraalRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection().registerType(TrialDataAspect.class, MemberCategory.values());
        hints.reflection().registerType(LoginTokenAspect.class, MemberCategory.values());
        hints.reflection().registerType(IPAddressAspect.class, MemberCategory.values());
        hints.reflection().registerType(QueryLogAspect.class, MemberCategory.values());
        hints.reflection().registerType(RequestLogAspect.class, MemberCategory.values());
        hints.reflection().registerType(CronVerificationAspect.class, MemberCategory.values());
    }
}

