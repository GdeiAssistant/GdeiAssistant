package cn.gdeiassistant.core.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class I18nTranslationFilter implements Filter {

    private final I18nTranslationService translationService;
    private final ObjectMapper objectMapper;

    public I18nTranslationFilter(I18nTranslationService translationService, ObjectMapper objectMapper) {
        this.translationService = translationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String acceptLang = httpRequest.getHeader("Accept-Language");
        if (acceptLang == null || acceptLang.isBlank() || acceptLang.startsWith("zh-CN") || acceptLang.equals("zh")) {
            chain.doFilter(request, response);
            return;
        }

        String path = httpRequest.getRequestURI();
        if (!I18nFieldConfig.isTranslatablePath(path)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);
        chain.doFilter(request, responseWrapper);

        String contentType = responseWrapper.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            responseWrapper.copyBodyToResponse();
            return;
        }

        byte[] body = responseWrapper.getContentAsByteArray();
        if (body.length == 0) {
            responseWrapper.copyBodyToResponse();
            return;
        }

        try {
            String targetLang = normalizeAcceptLanguage(acceptLang);
            List<String> fields = I18nFieldConfig.getFieldsForPath(path);
            JsonNode root = objectMapper.readTree(body);

            if (root.isObject()) {
                for (String fieldPath : fields) {
                    translateFieldPath((ObjectNode) root, fieldPath, targetLang);
                }
            }

            byte[] modifiedBody = objectMapper.writeValueAsBytes(root);
            httpResponse.setContentLength(modifiedBody.length);
            httpResponse.getOutputStream().write(modifiedBody);
        } catch (Exception e) {
            responseWrapper.copyBodyToResponse();
        }
    }

    private void translateFieldPath(ObjectNode root, String path, String targetLang) {
        if (path.contains("[]")) {
            String[] parts = path.split("\\[]\\.", 2);
            if (parts.length == 2) {
                JsonNode arrayNode = navigateTo(root, parts[0]);
                if (arrayNode != null && arrayNode.isArray()) {
                    for (JsonNode element : arrayNode) {
                        if (element.isObject()) {
                            translateSimpleField((ObjectNode) element, parts[1], targetLang);
                        } else if (element.isTextual()) {
                            // array of strings (e.g., description[])
                            translateArrayElements(root, parts[0], targetLang);
                            break;
                        }
                    }
                }
            } else {
                // path ends with [] — translate array of strings
                String arrayPath = path.replace("[]", "");
                translateArrayElements(root, arrayPath, targetLang);
            }
            return;
        }

        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0) {
            translateSimpleField(root, path, targetLang);
            return;
        }

        String parentPath = path.substring(0, lastDot);
        String fieldName = path.substring(lastDot + 1);
        JsonNode parent = navigateTo(root, parentPath);
        if (parent != null && parent.isObject()) {
            translateSimpleField((ObjectNode) parent, fieldName, targetLang);
        }
    }

    private void translateSimpleField(ObjectNode node, String fieldName, String targetLang) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || !fieldNode.isTextual()) return;

        String original = fieldNode.asText();
        if (original.isBlank()) return;

        String cached = translationService.getCachedTranslation(original, targetLang);
        if (cached != null) {
            node.set(fieldName, new TextNode(cached));
        } else {
            translationService.enqueueTranslation(original, targetLang);
        }
    }

    private void translateArrayElements(ObjectNode root, String arrayPath, String targetLang) {
        JsonNode arrayNode = navigateTo(root, arrayPath);
        if (arrayNode == null || !arrayNode.isArray()) return;

        ArrayNode array = (ArrayNode) arrayNode;
        for (int i = 0; i < array.size(); i++) {
            JsonNode elem = array.get(i);
            if (elem.isTextual() && !elem.asText().isBlank()) {
                String cached = translationService.getCachedTranslation(elem.asText(), targetLang);
                if (cached != null) {
                    array.set(i, new TextNode(cached));
                } else {
                    translationService.enqueueTranslation(elem.asText(), targetLang);
                }
            }
        }
    }

    private JsonNode navigateTo(JsonNode root, String dotPath) {
        JsonNode current = root;
        for (String segment : dotPath.split("\\.")) {
            if (current == null) return null;
            current = current.get(segment);
        }
        return current;
    }

    private String normalizeAcceptLanguage(String header) {
        if (header == null) return "en";
        String lang = header.split(",")[0].trim().split(";")[0].trim();
        switch (lang) {
            case "zh-HK": case "zh-Hant-HK": return "zh-HK";
            case "zh-TW": case "zh-Hant-TW": case "zh-Hant": return "zh-TW";
            case "ja": case "ja-JP": return "ja";
            case "ko": case "ko-KR": return "ko";
            case "en": case "en-US": case "en-GB": return "en";
            default:
                if (lang.startsWith("zh")) return "zh-CN";
                if (lang.startsWith("ja")) return "ja";
                if (lang.startsWith("ko")) return "ko";
                if (lang.startsWith("en")) return "en";
                return "en";
        }
    }
}
