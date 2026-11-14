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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final Path CORE = MAIN.resolve("core");
    private static final Pattern CORE_MAPPER_PATTERN = Pattern.compile(
            "cn\\.gdeiassistant\\.core\\.([a-zA-Z0-9_]+)\\.mapper\\.([A-Za-z0-9_*]+)");
    private static final Pattern CORE_MODULE_PATTERN = Pattern.compile(
            "cn\\.gdeiassistant\\.core\\.([a-zA-Z0-9_]+)\\.");

    private static final Set<String> LEGACY_CROSS_MODULE_MAPPER_IMPORTS = Set.of(
            "campuscredential/service/CampusCredentialService.java -> privacy.mapper.PrivacyMapper",
            "campuscredential/service/CampusCredentialService.java -> user.mapper.UserMapper",
            "cetquery/service/CetQueryService.java -> cet.mapper.CetMapper",
            "dataquery/service/DataQueryService.java -> electricfees.mapper.ElectricFeesMapper",
            "dataquery/service/DataQueryService.java -> yellowpage.mapper.YellowPageMapper",
            "deletion/service/AccountDeletionService.java -> user.mapper.UserMapper",
            "deletion/service/DeletionCleanupWorker.java -> close.mapper.CloseMapper",
            "deletion/service/DeletionCleanupWorker.java -> user.mapper.UserMapper",
            "gradequery/service/GradeCronService.java -> cron.mapper.CronMapper",
            "information/service/announcement/AnnouncementService.java -> announcement.mapper.AnnouncementMapper",
            "message/service/InteractionNotificationService.java -> user.mapper.UserMapper",
            "schedulequery/service/ScheduleCronService.java -> cron.mapper.CronMapper",
            "social/service/SocialIdentityService.java -> privacy.mapper.PrivacyMapper",
            "social/service/SocialIdentityService.java -> profile.mapper.ProfileMapper",
            "social/service/SocialIdentityService.java -> user.mapper.UserMapper",
            "social/service/SocialRelationService.java -> privacy.mapper.PrivacyMapper",
            "social/service/SocialRelationService.java -> user.mapper.UserMapper",
            "social/websocket/SocialRealtimeHandler.java -> user.mapper.UserMapper",
            "topic/service/TopicService.java -> profile.mapper.ProfileMapper",
            "topic/service/TopicService.java -> user.mapper.UserMapper",
            "user/service/PublicAuthorResolver.java -> profile.mapper.ProfileMapper",
            "userdata/service/UserDataService.java -> data.mapper.AppDataMapper",
            "userdata/service/UserDataService.java -> deletion.mapper.DeletionCleanupMapper",
            "userdata/service/UserDataService.java -> logdata.mapper.LogDataMapper",
            "userdata/service/UserDataService.java -> phone.mapper.PhoneMapper",
            "userdata/service/UserDataService.java -> privacy.mapper.PrivacyMapper",
            "userdata/service/UserDataService.java -> profile.mapper.ProfileMapper",
            "userdata/service/UserDataService.java -> user.mapper.UserMapper",
            "userlogin/service/UserCertificateService.java -> user.mapper.UserMapper"
    );

    private static final Set<String> LEGACY_BIDIRECTIONAL_CYCLES = Set.of(
            "announcement<->information",
            "campuscredential<->userlogin",
            "cron<->gradequery",
            "cron<->schedulequery",
            "dataquery<->yellowpage",
            "profile<->userprofile",
            "userdata<->userlogin"
    );

    private static final List<Map.Entry<String, Set<String>>> LEGACY_LIBRARY_IMPORTS = List.of(
            Map.entry("com.alibaba.fastjson2.", Set.of(
                    "common/pojo/entity/Charge.java",
                    "common/pojo/entity/Cookie.java",
                    "common/pojo/entity/User.java",
                    "common/tools/springutils/AliYunAPIUtils.java",
                    "common/tools/springutils/AliYunSMSUtils.java",
                    "core/capability/ai/AiModelSelection.java",
                    "core/capability/ai/AiProtocol.java",
                    "core/cardquery/service/CardQueryService.java",
                    "core/charge/pojo/dto/ChargeRequestDTO.java",
                    "core/charge/pojo/vo/ChargeOrderVO.java",
                    "core/charge/pojo/vo/ChargeVO.java",
                    "core/userdata/service/UserDataService.java",
                    "integration/library/LibraryClient.java"
            )),
            Map.entry("com.google.gson.", Set.of()),
            Map.entry("org.apache.http.impl.nio.", Set.of()),
            Map.entry("org.apache.http.nio.", Set.of())
    );

    /** Comments and string literals (pointcuts, @MapperScan packages) are not type dependencies. */
    private static String withoutCommentsAndStrings(String source) {
        StringBuilder out = new StringBuilder(source.length());
        int n = source.length();
        int i = 0;
        while (i < n) {
            char c = source.charAt(i);
            char next = i + 1 < n ? source.charAt(i + 1) : '\0';
            if (c == '/' && next == '*') {
                i += 2;
                while (i < n) {
                    if (source.charAt(i) == '*' && i + 1 < n && source.charAt(i + 1) == '/') {
                        i += 2;
                        break;
                    }
                    i++;
                }
            } else if (c == '/' && next == '/') {
                i += 2;
                while (i < n && source.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '"' && next == '"' && i + 2 < n && source.charAt(i + 2) == '"') {
                out.append("\"\"");
                i += 3;
                while (i < n) {
                    if (source.charAt(i) == '\\' && i + 1 < n) {
                        i += 2;
                    } else if (i + 2 < n && source.charAt(i) == '"' && source.charAt(i + 1) == '"'
                            && source.charAt(i + 2) == '"') {
                        i += 3;
                        break;
                    } else {
                        i++;
                    }
                }
            } else if (c == '"') {
                out.append("\"\"");
                i++;
                while (i < n) {
                    char sc = source.charAt(i++);
                    if (sc == '\\' && i < n) {
                        i++;
                    } else if (sc == '"') {
                        break;
                    }
                }
            } else if (c == '\'') {
                out.append("''");
                i++;
                while (i < n) {
                    char cc = source.charAt(i++);
                    if (cc == '\\' && i < n) {
                        i++;
                    } else if (cc == '\'') {
                        break;
                    }
                }
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
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

    @Test
    void coreModulesDoNotAddCrossModuleMapperDependencies() throws IOException {
        Set<String> actual = new TreeSet<>();
        for (Path file : javaFilesUnder(CORE)) {
            Path relative = CORE.relativize(file);
            if (relative.getNameCount() < 2) {
                continue;
            }
            String module = relative.getName(0).toString();
            String relativePath = relative.toString().replace('\\', '/');
            String stripped = withoutCommentsAndStrings(Files.readString(file));
            Matcher matcher = CORE_MAPPER_PATTERN.matcher(stripped);
            while (matcher.find()) {
                String otherModule = matcher.group(1);
                if (!otherModule.equals(module)) {
                    actual.add(relativePath + " -> " + otherModule + ".mapper." + matcher.group(2));
                }
            }
        }
        assertMatchesBaseline(
                "core modules must not add cross-module MyBatis mapper dependencies",
                actual,
                LEGACY_CROSS_MODULE_MAPPER_IMPORTS);
    }

    @Test
    void coreModulesDoNotAddBidirectionalPackageCycles() throws IOException {
        Map<String, Set<String>> dependencies = new HashMap<>();
        for (Path file : javaFilesUnder(CORE)) {
            Path relative = CORE.relativize(file);
            if (relative.getNameCount() < 2) {
                continue;
            }
            String module = relative.getName(0).toString();
            String stripped = withoutCommentsAndStrings(Files.readString(file));
            Matcher matcher = CORE_MODULE_PATTERN.matcher(stripped);
            while (matcher.find()) {
                String otherModule = matcher.group(1);
                if (!otherModule.equals(module)) {
                    dependencies.computeIfAbsent(module, ignored -> new TreeSet<>()).add(otherModule);
                }
            }
        }
        Set<String> actualCycles = new TreeSet<>();
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            String a = entry.getKey();
            for (String b : entry.getValue()) {
                if (dependencies.getOrDefault(b, Set.of()).contains(a)) {
                    actualCycles.add(a.compareTo(b) < 0 ? a + "<->" + b : b + "<->" + a);
                }
            }
        }
        assertMatchesBaseline(
                "core modules must not introduce new bidirectional package cycles",
                actualCycles,
                LEGACY_BIDIRECTIONAL_CYCLES);
    }

    @Test
    void noNewLegacyHttpOrJsonLibraryImports() throws IOException {
        List<Path> mainFiles = javaFilesUnder(MAIN);
        for (Map.Entry<String, Set<String>> entry : LEGACY_LIBRARY_IMPORTS) {
            String prefix = entry.getKey();
            Set<String> actual = new TreeSet<>();
            for (Path file : mainFiles) {
                String stripped = withoutCommentsAndStrings(Files.readString(file));
                if (stripped.contains(prefix)) {
                    actual.add(MAIN.relativize(file).toString().replace('\\', '/'));
                }
            }
            assertMatchesBaseline(
                    "new references to legacy library " + prefix + " are forbidden",
                    actual,
                    entry.getValue());
        }
    }

    private static List<Path> javaFilesUnder(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }
    }

    private static void assertMatchesBaseline(String rule, Set<String> actual, Set<String> baseline) {
        Set<String> unexpected = new TreeSet<>(actual);
        unexpected.removeAll(baseline);
        assertTrue(unexpected.isEmpty(), rule + " (unexpected): " + unexpected);

        Set<String> stale = new TreeSet<>(baseline);
        stale.removeAll(actual);
        assertTrue(stale.isEmpty(), rule + " (resolved legacy entries to remove from baseline): " + stale);
    }

    private static String sourceOf(String relative) {
        try {
            return Files.readString(MAIN.resolve(relative));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
