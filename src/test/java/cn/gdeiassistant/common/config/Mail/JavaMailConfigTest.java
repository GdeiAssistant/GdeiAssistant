package cn.gdeiassistant.common.config.Mail;

import cn.gdeiassistant.common.enums.Module.CoreModuleEnum;
import cn.gdeiassistant.common.enums.Module.ModuleEnum;
import cn.gdeiassistant.common.tools.SpringUtils.ModuleUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JavaMailConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JavaMailConfig.class, TestSupportConfig.class);

    @Test
    void missingSmtpConfigurationSkipsMailSenderAndDisablesEmailModule() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(JavaMailSenderImpl.class);
            assertThat(context.getBean(ModuleUtils.class).CheckModuleState(ModuleEnum.EMAIL)).isFalse();
        });
    }

    @Test
    void completeSmtpConfigurationCreatesMailSender() {
        contextRunner.withPropertyValues(
                "email.smtp.host=smtp.example.com",
                "email.smtp.port=465",
                "email.smtp.username=test@example.com",
                "email.smtp.password=secret",
                "email.smtp.auth=true"
        ).run(context -> {
            assertThat(context).hasSingleBean(JavaMailSenderImpl.class);
            assertThat(context.getBean(ModuleUtils.class).CheckModuleState(ModuleEnum.EMAIL)).isTrue();
        });
    }

    @TestConfiguration
    static class TestSupportConfig {

        @Bean("moduleStateMap")
        Map<ModuleEnum, Boolean> moduleStateMap() {
            return new EnumMap<>(ModuleEnum.class);
        }

        @Bean("coreModuleStateMap")
        Map<CoreModuleEnum, Boolean> coreModuleStateMap() {
            return new EnumMap<>(CoreModuleEnum.class);
        }

        @Bean
        ModuleUtils moduleUtils() {
            return new ModuleUtils();
        }
    }
}
