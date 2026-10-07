package cn.gdeiassistant.core.i18n;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class I18nTranslationService {

    private static final Logger log = LoggerFactory.getLogger(I18nTranslationService.class);

    private final I18nConfig config;
    private RestTemplate restTemplate = cn.gdeiassistant.common.tools.springutils.OutboundHttpClients.create(3000, 6000);
    @Autowired(required=false)
    public void configureHttp(cn.gdeiassistant.common.config.application.OutboundIntegrationProperties settings) {
        restTemplate = cn.gdeiassistant.common.tools.springutils.OutboundHttpClients.create(settings.getConnectTimeoutMs(), settings.getReadTimeoutMs());
    }

    private final ConcurrentHashMap<String, CompletableFuture<String>> inFlight = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired(required = false)
    @Qualifier("i18nExecutor")
    private Executor i18nExecutor;

    private static final Map<String, String> DEEPL_LANG_MAP = Map.of(
            "en", "EN",
            "ja", "JA",
            "ko", "KO",
            "zh-HK", "ZH-HANT",
            "zh-TW", "ZH-HANT"
    );

    public I18nTranslationService(I18nConfig config) {
        this.config = config;
    }

    public String getCachedTranslation(String text, String targetLang) {
        String key = cacheKey(targetLang, text);
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis lookup failed for i18n cache: {}", e.getMessage());
            return null;
        }
    }

    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank() || targetLang == null || targetLang.isBlank()) {
            return null;
        }

        String cached = getCachedTranslation(text, targetLang);
        if (cached != null) {
            return cached;
        }

        String translated = callDeepL(text, targetLang);
        if (translated != null && !translated.isBlank()) {
            storeInCache(text, targetLang, translated);
            return translated;
        }
        return null;
    }

    public void enqueueTranslation(String text, String targetLang) {
        if (text == null || text.isBlank() || targetLang == null || i18nExecutor == null) return;

        String dedupKey = targetLang + ":" + normalizeText(text);
        CompletableFuture<String> existing = inFlight.putIfAbsent(dedupKey, new CompletableFuture<>());
        if (existing != null) return; // already in flight

        try {
            i18nExecutor.execute(() -> {
                try {
                    String result = callDeepL(text, targetLang);
                    if (result != null) {
                        storeInCache(text, targetLang, result);
                    }
                } catch (Exception e) {
                    log.warn("i18n translation failed for lang={}: {}", targetLang, e.getMessage());
                } finally {
                    inFlight.remove(dedupKey);
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            inFlight.remove(dedupKey);
            log.warn("i18n queue full; retaining original text");
        }
    }

    private String callDeepL(String text, String targetLang) {
        String deeplLang = DEEPL_LANG_MAP.get(targetLang);
        if (deeplLang == null) return null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "DeepL-Auth-Key " + config.getDeeplApiKey());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("text", text);
        body.add("target_lang", deeplLang);
        body.add("tag_handling", "html");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            if (Thread.currentThread().isInterrupted()) return null;
            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        config.getDeeplApiUrl(), HttpMethod.POST, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<Map<String, String>> translations = (List<Map<String, String>>) response.getBody().get("translations");
                    if (translations != null && !translations.isEmpty()) {
                        String translated = translations.get(0).get("text");
                        return postProcessTraditionalChinese(translated, targetLang);
                    }
                }
                return null;
            } catch (Exception e) {
                if (attempt < maxRetries - 1) {
                    try { Thread.sleep((long) Math.pow(2, attempt) * 1000); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); return null; }
                } else {
                    log.warn("DeepL API call failed after {} retries: {}", maxRetries, e.getMessage());
                }
            }
        }
        return null;
    }

    private String postProcessTraditionalChinese(String text, String targetLang) {
        if (text == null) return null;
        if ("zh-HK".equals(targetLang) || "zh-TW".equals(targetLang)) {
            return ZhConverterUtil.toTraditional(text);
        }
        return text;
    }

    private void storeInCache(String originalText, String targetLang, String translated) {
        String key = cacheKey(targetLang, originalText);
        try {
            redisTemplate.opsForValue().set(key, translated, config.getCacheTtlDays(), TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis store failed for i18n cache: {}", e.getMessage());
        }
    }

    static String normalizeText(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text.trim(), Normalizer.Form.NFC);
    }

    private String cacheKey(String lang, String text) {
        String normalized = normalizeText(text);
        String hash = sha256(normalized);
        return "i18n:" + lang + ":" + hash;
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : encoded) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(text.hashCode());
        }
    }
}
