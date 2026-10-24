package cn.gdeiassistant.core.capability.ai;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriUtils;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/** Wire protocol varies independently from the platform and its processing region. */
public enum AiProtocol {
    OPENAI_CHAT, OPENAI_RESPONSES, ANTHROPIC, GEMINI;

    public HttpHeaders headers(AiProperties.Endpoint endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (endpoint.getAuthentication() == AiProperties.Authentication.BEARER) {
            headers.setBearerAuth(endpoint.getApiKey());
        } else if (this == ANTHROPIC) {
            headers.set("x-api-key", endpoint.getApiKey());
        } else if (this == GEMINI) {
            headers.set("x-goog-api-key", endpoint.getApiKey());
        } else {
            headers.setBearerAuth(endpoint.getApiKey());
        }
        if (this == ANTHROPIC) headers.set("anthropic-version", "2023-06-01");
        return headers;
    }

    public URI inferenceUri(AiProperties.Endpoint endpoint, String model) {
        String path = switch (this) {
            case OPENAI_CHAT -> "/chat/completions";
            case OPENAI_RESPONSES -> "/responses";
            case ANTHROPIC -> "/messages";
            case GEMINI -> "/models/" + UriUtils.encodePathSegment(model, StandardCharsets.UTF_8) + ":generateContent";
        };
        return URI.create(endpoint.getBaseUrl() + path);
    }

    public JSONObject request(String model, AiRequest request) {
        JSONObject body = new JSONObject();
        JSONArray messages = new JSONArray();
        StringBuilder system = new StringBuilder();
        for (AiRequest.Message message : request.messages()) {
            if (message.role() == AiRequest.Role.SYSTEM && (this == ANTHROPIC || this == GEMINI)) {
                if (!system.isEmpty()) system.append("\n");
                system.append(message.text());
                continue;
            }
            JSONArray content = new JSONArray();
            if (!message.text().isBlank()) {
                content.add(this == GEMINI ? JSONObject.of("text", message.text()) : JSONObject.of(
                        "type", this == OPENAI_RESPONSES ? "input_text" : "text", "text", message.text()));
            }
            for (AiRequest.Image image : message.images()) {
                content.add(switch (this) {
                    case OPENAI_RESPONSES -> JSONObject.of("type", "input_image", "image_url", dataUrl(image));
                    case OPENAI_CHAT -> JSONObject.of("type", "image_url", "image_url", JSONObject.of("url", dataUrl(image)));
                    case ANTHROPIC -> JSONObject.of("type", "image", "source", JSONObject.of("type", "base64",
                            "media_type", image.mediaType(), "data", image.base64()));
                    case GEMINI -> JSONObject.of("inlineData", JSONObject.of("mimeType", image.mediaType(), "data", image.base64()));
                });
            }
            String role = message.role().name().toLowerCase(java.util.Locale.ROOT);
            if (this == GEMINI && message.role() == AiRequest.Role.ASSISTANT) role = "model";
            messages.add(JSONObject.of("role", role, this == GEMINI ? "parts" : "content",
                    this == OPENAI_RESPONSES && message.images().isEmpty() ? message.text() : content));
        }
        switch (this) {
            case OPENAI_RESPONSES -> {
                body.put("model", model); body.put("input", messages); body.put("store", false);
                body.put("max_output_tokens", request.maxOutputTokens());
            }
            case OPENAI_CHAT -> {
                body.put("model", model); body.put("messages", messages); body.put("max_tokens", request.maxOutputTokens());
            }
            case ANTHROPIC -> {
                body.put("model", model); body.put("messages", messages); body.put("max_tokens", request.maxOutputTokens());
                if (!system.isEmpty()) body.put("system", system.toString());
            }
            case GEMINI -> {
                body.put("contents", messages);
                body.put("generationConfig", JSONObject.of("maxOutputTokens", request.maxOutputTokens()));
                if (!system.isEmpty()) body.put("systemInstruction", JSONObject.of("parts", new JSONArray(JSONObject.of("text", system.toString()))));
            }
        }
        return body;
    }

    private static String dataUrl(AiRequest.Image image) {
        return "data:" + image.mediaType() + ";base64," + image.base64();
    }

    public String text(String response) {
        if (response == null || response.isBlank()) return "";
        JSONObject body = JSONObject.parseObject(response);
        return switch (this) {
            case OPENAI_RESPONSES -> {
                StringBuilder result = new StringBuilder();
                JSONArray output = body.getJSONArray("output");
                if (output != null) for (int i = 0; i < output.size(); i++) {
                    JSONObject item = output.getJSONObject(i);
                    if ("message".equals(item.getString("type"))) result.append(parts(item.getJSONArray("content"), "output_text", false));
                }
                yield result.toString();
            }
            case ANTHROPIC -> parts(body.getJSONArray("content"), "text", false);
            case OPENAI_CHAT -> {
                JSONArray choices = body.getJSONArray("choices");
                JSONObject message = choices == null || choices.isEmpty() ? null : choices.getJSONObject(0).getJSONObject("message");
                Object content = message == null ? null : message.get("content");
                yield content instanceof String value ? value : parts(content instanceof JSONArray array ? array : null, "text", false);
            }
            case GEMINI -> {
                JSONArray candidates = body.getJSONArray("candidates");
                JSONObject content = candidates == null || candidates.isEmpty() ? null : candidates.getJSONObject(0).getJSONObject("content");
                yield parts(content == null ? null : content.getJSONArray("parts"), null, true);
            }
        };
    }

    private static String parts(JSONArray items, String type, boolean excludeThoughts) {
        StringBuilder result = new StringBuilder();
        if (items != null) for (int i = 0; i < items.size(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item == null || (type != null && !type.equals(item.getString("type")))
                    || (excludeThoughts && Boolean.TRUE.equals(item.getBoolean("thought")))) continue;
            String text = item.getString("text");
            if (text != null) result.append(text);
        }
        return result.toString();
    }
}
