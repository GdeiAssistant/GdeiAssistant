package cn.gdeiassistant.core.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        String targetLang = normalizeAcceptLanguage(acceptLang);
        if ("zh-CN".equals(targetLang)) {
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
            List<String> fields = I18nFieldConfig.getFieldsForPath(path);
            JsonNode root = objectMapper.readTree(body);

            for (String fieldPath : fields) {
                translateFieldPath(root, fieldPath, targetLang);
            }

            byte[] modifiedBody = objectMapper.writeValueAsBytes(root);
            httpResponse.setContentLength(modifiedBody.length);
            httpResponse.getOutputStream().write(modifiedBody);
        } catch (Exception e) {
            responseWrapper.copyBodyToResponse();
        }
    }

    private void translateFieldPath(JsonNode root, String path, String targetLang) {
        translateBySegments(root, path.split("\\."), 0, targetLang);
    }

    private void translateBySegments(JsonNode currentNode, String[] segments, int index, String targetLang) {
        if (currentNode == null || index >= segments.length) {
            return;
        }

        String segment = segments[index];
        boolean isArraySegment = segment.endsWith("[]");
        String fieldName = isArraySegment ? segment.substring(0, segment.length() - 2) : segment;

        if (isArraySegment) {
            JsonNode arrayNode = resolveChildNode(currentNode, fieldName);
            if (arrayNode == null || !arrayNode.isArray()) {
                return;
            }
            for (JsonNode element : arrayNode) {
                if (index == segments.length - 1) {
                    translateTextNodeInArray(arrayNode, element, targetLang);
                } else {
                    translateBySegments(element, segments, index + 1, targetLang);
                }
            }
            return;
        }

        if (index == segments.length - 1) {
            translateSimpleField(currentNode, fieldName, targetLang);
            return;
        }

        translateBySegments(resolveChildNode(currentNode, fieldName), segments, index + 1, targetLang);
    }

    private JsonNode resolveChildNode(JsonNode currentNode, String fieldName) {
        if (currentNode == null) {
            return null;
        }
        if (fieldName == null || fieldName.isEmpty()) {
            return currentNode;
        }
        if (!currentNode.isObject()) {
            return null;
        }
        return currentNode.get(fieldName);
    }

    private void translateSimpleField(JsonNode node, String fieldName, String targetLang) {
        if (!(node instanceof ObjectNode objectNode)) {
            return;
        }
        JsonNode fieldNode = objectNode.get(fieldName);
        if (fieldNode == null || !fieldNode.isTextual()) {
            return;
        }
        translateAndReplace(objectNode, fieldName, fieldNode.asText(), targetLang);
    }

    private void translateTextNodeInArray(JsonNode arrayNode, JsonNode element, String targetLang) {
        if (!arrayNode.isArray() || !element.isTextual()) {
            return;
        }
        String original = element.asText();
        if (original.isBlank()) {
            return;
        }
        String cached = translationService.getCachedTranslation(original, targetLang);
        if (cached != null) {
            for (int i = 0; i < arrayNode.size(); i++) {
                if (arrayNode.get(i) == element) {
                    ((com.fasterxml.jackson.databind.node.ArrayNode) arrayNode).set(i, new TextNode(cached));
                    break;
                }
            }
        } else {
            translationService.enqueueTranslation(original, targetLang);
        }
    }

    private void translateAndReplace(ObjectNode node, String fieldName, String original, String targetLang) {
        if (original.isBlank()) {
            return;
        }
        String cached = translationService.getCachedTranslation(original, targetLang);
        if (cached != null) {
            node.set(fieldName, new TextNode(cached));
        } else {
            translationService.enqueueTranslation(original, targetLang);
        }
    }

    private String normalizeAcceptLanguage(String header) {
        if (header == null || header.isBlank()) return "zh-CN";
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
                return "zh-CN";
        }
    }
}
