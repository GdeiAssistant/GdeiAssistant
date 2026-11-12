package cn.gdeiassistant.architecture;

import cn.gdeiassistant.common.spi.IPAddressRecorder;
import cn.gdeiassistant.common.spi.LoginTokenValidator;
import cn.gdeiassistant.common.spi.SessionCertificateProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The common and integration layers used to reach back into core for services and
 * entities, which made them impossible to reuse and hid the real dependency direction.
 * common now declares narrow SPI contracts; core implements them.
 */
class LayerDependencyGuardTest {

    private static final Path MAIN = Paths.get("src/main/java/cn/gdeiassistant");

    /** Comments and string literals (pointcuts, @MapperScan packages) are not type dependencies. */
    private static String withoutCommentsAndStrings(String source) {
        String s = source.replaceAll("/\\*.*?\\*/", "");
        s = s.replaceAll("//[^\\n]*", "");
        return s.replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"\"");
    }

    @Test
    void commonAndIntegrationDoNotReferenceCoreTypes() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (String layer : List.of("common", "integration")) {
            Path root = MAIN.resolve(layer);
            try (Stream<Path> files = Files.walk(root)) {
                Iterable<Path> iterable = files.filter(p -> p.toString().endsWith(".java"))::iterator;
                for (Path file : iterable) {
                    String stripped = withoutCommentsAndStrings(Files.readString(file));
                    if (stripped.contains("cn.gdeiassistant.core.")) {
                        offenders.add(MAIN.relativize(file).toString());
                    }
                }
            }
        }
        assertTrue(offenders.isEmpty(),
                "common/integration must depend on core only through SPI contracts: " + offenders);
    }

    @Test
    void coreServicesImplementTheCommonSpiContracts() throws ClassNotFoundException {
        assertTrue(SessionCertificateProvider.class.isAssignableFrom(
                Class.forName("cn.gdeiassistant.core.userlogin.service.UserCertificateService")));
        assertTrue(IPAddressRecorder.class.isAssignableFrom(
                Class.forName("cn.gdeiassistant.core.ipaddress.service.IPAddressService")));
        assertTrue(LoginTokenValidator.class.isAssignableFrom(
                Class.forName("cn.gdeiassistant.core.token.service.LoginTokenService")));
    }

    @Test
    void theServletFilterNoLongerHoldsAMyBatisMapper() throws Exception {
        try {
            Class.forName("cn.gdeiassistant.common.filter.JwtSessionIdFilter")
                    .getDeclaredField("userMapper");
            throw new AssertionError("JwtSessionIdFilter must not reach into the persistence layer");
        } catch (NoSuchFieldException expected) {
            // the account-state check belongs to the userlogin domain
        }
        assertFalse(withoutCommentsAndStrings(sourceOf(
                "common/filter/JwtSessionIdFilter.java"))
                .contains("UserMapper"));
    }

    @Test
    void sharedDataTypesHaveExactlyOneHomeInCommon() {
        for (String fqn : List.of(
                "cn.gdeiassistant.common.pojo.entity.UserCertificateEntity",
                "cn.gdeiassistant.common.pojo.delaytask.DelayTask",
                "cn.gdeiassistant.common.pojo.delaytask.DelayTaskElement",
                "cn.gdeiassistant.common.pojo.delaytask.SessionAttributeExpireDelayTaskElement")) {
            try {
                Class.forName(fqn);
            } catch (ClassNotFoundException e) {
                throw new AssertionError("Missing shared data type " + fqn, e);
            }
        }
        for (String stale : List.of(
                "core/userlogin/pojo/entity/UserCertificateEntity.java",
                "core/delaytask/pojo/DelayTask.java",
                "core/delaytask/pojo/DelayTaskElement.java",
                "core/delaytask/pojo/SessionAttributeExpireDelayTaskElement.java")) {
            assertFalse(Files.exists(MAIN.resolve(stale)), "Duplicated data type left behind: " + stale);
        }
    }

    private static String sourceOf(String relative) {
        try {
            return Files.readString(MAIN.resolve(relative));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
