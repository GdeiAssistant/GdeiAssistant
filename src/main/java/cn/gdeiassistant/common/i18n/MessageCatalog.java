package cn.gdeiassistant.common.i18n;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Backend API message catalog.
 *
 * <p>The catalog lives in {@code src/main/resources/i18n/messages_*.json}: a message is
 * identified by its key, and every key carries six locales. Code refers to the key, never to the
 * Chinese wording, so rewording a message no longer silently drops its translations.
 *
 * <p>The catalog is stored as JSON rather than {@code .properties} because it is data, not
 * configuration: the secret scanner treats any {@code .properties} assignment whose key mentions a
 * token or a password as a credential, and every {@code auth.passwordIncorrect}-style message key
 * would be reported as one.
 *
 * <p>{@code zh-HK}/{@code zh-TW} entries may be omitted for a key: they are derived from the
 * simplified text with OpenCC. Hand-written entries always take precedence.
 */
public final class MessageCatalog {

    public static final List<String> LOCALES = List.of("zh-CN", "zh-HK", "zh-TW", "en", "ja", "ko");

    private static final String RESOURCE_PREFIX = "/i18n/messages_";

    /** Fails on a duplicated key instead of letting the last one win silently. */
    private static final ObjectMapper MESSAGES =
            new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)}");


    private static final Map<String, Map<String, String>> TRANSLATIONS = load();
    private static final Map<String, String> TEXT_TO_KEY = buildTextToKey();
    private static final List<TemplateEntry> TEMPLATES = buildTemplates();

    private record TemplateEntry(String key, Pattern pattern, int specificity) {
    }

    private MessageCatalog() {
    }

    private static Map<String, Map<String, String>> load() {
        Map<String, Map<String, String>> perLocale = new LinkedHashMap<>();
        for (String locale : LOCALES) {
            String resource = RESOURCE_PREFIX + locale.replace('-', '_') + ".json";
            try (InputStream in = MessageCatalog.class.getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IllegalStateException("Missing message catalog resource: " + resource);
                }
                perLocale.put(locale,
                        MESSAGES.readValue(in, new TypeReference<LinkedHashMap<String, String>>() { }));
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to read " + resource, e);
            }
        }

        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        Map<String, String> base = perLocale.get("zh-CN");
        for (String key : base.keySet()) {
            String zhCn = base.get(key);
            Map<String, String> entry = new HashMap<>();
            for (String locale : LOCALES) {
                String value = perLocale.get(locale).get(key);
                if (value == null && ("zh-HK".equals(locale) || "zh-TW".equals(locale))) {
                    value = ZhConverterUtil.toTraditional(zhCn);
                }
                entry.put(locale, value == null ? zhCn : value);
            }
            result.put(key, Collections.unmodifiableMap(entry));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, String> buildTextToKey() {
        Map<String, String> result = new HashMap<>();
        TRANSLATIONS.forEach((key, entry) -> {
            String text = entry.get("zh-CN");
            if (!PLACEHOLDER.matcher(text).find()) {
                result.put(text, key);
            }
        });
        return Collections.unmodifiableMap(result);
    }

    private static List<TemplateEntry> buildTemplates() {
        List<TemplateEntry> result = new ArrayList<>();
        TRANSLATIONS.forEach((key, entry) -> {
            String text = entry.get("zh-CN");
            Matcher matcher = PLACEHOLDER.matcher(text);
            StringBuilder regex = new StringBuilder("^");
            int cursor = 0;
            int specificity = 0;
            while (matcher.find()) {
                String literal = text.substring(cursor, matcher.start());
                specificity += literal.length();
                regex.append(Pattern.quote(literal)).append("(.*?)");
                cursor = matcher.end();
            }
            String tail = text.substring(cursor);
            specificity += tail.length();
            regex.append(Pattern.quote(tail)).append("$");
            if (specificity > 0) {
                result.add(new TemplateEntry(key, Pattern.compile(regex.toString(), Pattern.DOTALL), specificity));
            }
        });
        result.sort((a, b) -> Integer.compare(b.specificity(), a.specificity()));
        return Collections.unmodifiableList(result);
    }

    /** Localizes a Chinese message produced by legacy code (exception messages, stored text). */
    public static String localizeMessage(String message, String language) {
        if (message == null || message.isBlank()) {
            return message;
        }
        String normalizedLanguage = ApiLanguageResolver.normalizeLanguage(language);
        if ("zh-CN".equals(normalizedLanguage)) {
            return message;
        }
        String key = TEXT_TO_KEY.get(message);
        if (key != null) {
            return translate(key, normalizedLanguage, message);
        }
        for (TemplateEntry entry : TEMPLATES) {
            Matcher matcher = entry.pattern().matcher(message);
            if (matcher.matches()) {
                Object[] args = new Object[matcher.groupCount()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = matcher.group(i + 1);
                }
                return format(entry.key(), normalizedLanguage, args);
            }
        }
        if ("zh-HK".equals(normalizedLanguage) || "zh-TW".equals(normalizedLanguage)) {
            return ZhConverterUtil.toTraditional(message);
        }
        return message;
    }

    /** Renders a catalog key directly in the requested language. */
    public static String get(String key, String language, Object... args) {
        String text = translate(key, ApiLanguageResolver.normalizeLanguage(language), key);
        return formatText(text, args);
    }

    /** Renders a catalog key in Simplified Chinese; used where an exception carries the text. */
    public static String text(String key, Object... args) {
        return get(key, "zh-CN", args);
    }

    public static String translate(String key, String language, String fallback) {
        Map<String, String> translations = TRANSLATIONS.get(key);
        if (translations == null) {
            return fallback;
        }
        return translations.getOrDefault(language, translations.getOrDefault("zh-CN", fallback));
    }

    public static String format(String key, String language, Object... args) {
        String template = translate(key, language, key);
        return formatText(template, args);
    }

    private static String formatText(String template, Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }
        return MessageFormat.format(template.replace("'", "''"), args);
    }

    public static boolean hasKey(String key) {
        return TRANSLATIONS.containsKey(key);
    }

    public static Set<String> keys() {
        return TRANSLATIONS.keySet();
    }

    public static Map<String, Map<String, String>> translations() {
        return TRANSLATIONS;
    }
}
