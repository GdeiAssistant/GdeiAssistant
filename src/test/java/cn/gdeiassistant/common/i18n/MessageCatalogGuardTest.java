package cn.gdeiassistant.common.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Build-time guard for the message catalog. The regression this prevents: 224 distinct Chinese
 * exception messages (363 throw sites) had no translation at all, so every non-Chinese user saw
 * Simplified Chinese. Adding a user-facing string without a catalog entry must now fail the build.
 */
class MessageCatalogGuardTest {

    private static final Set<String> LOCALES = Set.of("zh-CN", "zh-HK", "zh-TW", "en", "ja", "ko");
    private static final Pattern CHINESE = Pattern.compile("[\\u4e00-\\u9fff]");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\d+}");
    private static final Pattern EXCEPTION_LITERAL =
            Pattern.compile("new\\s+\\w*(?:Exception|Error)\\(\\s*\"([^\"]*)\"");
    private static final Pattern KEY_CALL = Pattern.compile(
            "(?:Messages\\.(?:get|forLanguage)|MessageCatalog\\.(?:get|format|translate))\\(\\s*\"([^\"]+)\"");

    private static String mainSources() throws IOException {
        Path root = Path.of(System.getProperty("user.dir"));
        while (root != null && !Files.isDirectory(root.resolve("src/main/java"))) {
            root = root.getParent();
        }
        assertNotNull(root, "unable to locate src/main/java from the test working directory");
        StringBuilder source = new StringBuilder();
        try (Stream<Path> paths = Files.walk(root.resolve("src/main/java"))) {
            List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
            assertTrue(javaFiles.size() > 100, "expected the full backend source tree");
            for (Path file : javaFiles) {
                source.append(Files.readString(file)).append('\n');
            }
        }
        return source.toString();
    }

    @Test
    void everyKeyHasSixNonEmptyTranslationsAndMatchingPlaceholders() {
        Map<String, Map<String, String>> catalog = MessageCatalog.translations();
        assertFalse(catalog.isEmpty());
        catalog.forEach((key, entry) -> {
            assertEquals(LOCALES, entry.keySet(), key);
            Set<String> expected = placeholders(entry.get("zh-CN"));
            entry.forEach((locale, text) -> {
                assertFalse(text.isBlank(), key + ":" + locale);
                assertEquals(expected, placeholders(text), key + ":" + locale);
            });
        });
    }

    @Test
    void noTwoKeysShareTheSameSimplifiedText() {
        Set<String> seen = new HashSet<>();
        MessageCatalog.translations().forEach((key, entry) ->
                assertTrue(seen.add(entry.get("zh-CN")),
                        "duplicate zh-CN text for " + key + ": " + entry.get("zh-CN")));
    }

    @Test
    void noChineseLiteralIsLeftInAnExceptionConstructor() throws IOException {
        List<String> offenders = new ArrayList<>();
        Matcher matcher = EXCEPTION_LITERAL.matcher(mainSources());
        while (matcher.find()) {
            if (CHINESE.matcher(matcher.group(1)).find()) {
                offenders.add(matcher.group(1));
            }
        }
        assertTrue(offenders.isEmpty(),
                "use Messages.get(key) instead of a Chinese literal: " + new TreeSet<>(offenders));
    }

    @Test
    void everyKeyUsedInCodeExistsInTheCatalog() throws IOException {
        String source = mainSources();
        Set<String> referenced = new HashSet<>();
        Matcher matcher = KEY_CALL.matcher(source);
        while (matcher.find()) {
            referenced.add(matcher.group(1));
        }
        assertTrue(referenced.size() > 200, "expected key-based message calls, found " + referenced.size());
        Set<String> unknown = new TreeSet<>(referenced);
        unknown.removeAll(MessageCatalog.keys());
        assertTrue(unknown.isEmpty(), "unknown message keys: " + unknown);
    }

    @Test
    void everyCatalogKeyIsReachableFromCode() throws IOException {
        String source = mainSources();
        List<String> deadKeys = new ArrayList<>();
        MessageCatalog.translations().forEach((key, entry) -> {
            String zhCn = entry.get("zh-CN");
            boolean template = PLACEHOLDER.matcher(zhCn).find();
            boolean byKey = source.contains("\"" + key + "\"");
            boolean byLegacyText = source.contains("\"" + zhCn + "\"");
            // A template is reached through pattern matching, never through an exact literal.
            assertTrue(template || byKey || byLegacyText,
                    "catalog entry is not used anywhere: " + key + " / " + zhCn);
            if (!template && !byKey && !byLegacyText) {
                deadKeys.add(key);
            }
        });
        assertTrue(deadKeys.isEmpty());
    }

    @Test
    void noMessageFallsBackToChineseForEnJaKo() {
        Object[] args = {"A", "B", "C"};
        MessageCatalog.keys().forEach(key -> {
            String zhCn = MessageCatalog.text(key, args);
            for (String locale : List.of("en", "ja", "ko")) {
                String translated = MessageCatalog.get(key, locale, args);
                assertFalse(zhCn.equals(translated), key + " has no " + locale + " translation");
            }
        });
    }

    @Test
    void legacyTextLookupResolvesEveryCatalogEntry() {
        MessageCatalog.translations().forEach((key, entry) -> {
            String zhCn = entry.get("zh-CN");
            if (PLACEHOLDER.matcher(zhCn).find()) {
                return;
            }
            for (String locale : List.of("en", "ja", "ko")) {
                assertEquals(entry.get(locale),
                        MessageCatalog.localizeMessage(zhCn, locale),
                        key + ":" + locale + " was not resolved from its Chinese text");
            }
        });
    }

    @Test
    void noCatalogValueLooksLikeACredential() throws IOException {
        // The catalog is JSON, which the secret scanner's configuration-assignment rule does not
        // cover, so the build carries its own check. Each resource is read directly: a value added
        // only to a non-zh-CN file would never surface through the merged catalog.
        Pattern credential = Pattern.compile(
                "(?i)(?:gh[pousr]_[A-Za-z0-9]{20,}|AKIA[0-9A-Z]{16}|[A-Za-z0-9+/]{40,}={0,2})");
        ObjectMapper mapper = new ObjectMapper();
        for (String locale : MessageCatalog.LOCALES) {
            String resource = "/i18n/messages_" + locale.replace('-', '_') + ".json";
            try (InputStream in = MessageCatalogGuardTest.class.getResourceAsStream(resource)) {
                assertNotNull(in, resource);
                Map<String, String> entries =
                        mapper.readValue(in, new TypeReference<LinkedHashMap<String, String>>() { });
                entries.forEach((key, text) -> assertFalse(credential.matcher(text).find(),
                        resource + " " + key + " looks like a credential"));
            }
        }
    }

    private static Set<String> placeholders(String text) {
        Set<String> result = new TreeSet<>();
        PLACEHOLDER.matcher(text).results().forEach(match -> result.add(match.group()));
        return result;
    }
}
