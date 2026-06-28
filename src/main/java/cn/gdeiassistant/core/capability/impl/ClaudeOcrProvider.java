package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.capability.ServiceProvider;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Order(4)
public class ClaudeOcrProvider implements ServiceProvider<OcrRequest, String> {

    @Autowired
    private RestTemplate restTemplate;

    private String apiKey;
    private String model;
    private String apiUrl;

    @Value("${api.ai.ocr.claude.api-key:}")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Value("${api.ai.ocr.claude.model:claude-sonnet-4-20250514}")
    public void setModel(String model) {
        this.model = model;
    }

    @Value("${api.ai.ocr.claude.api-url:https://api.anthropic.com/v1/messages}")
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public String providerName() {
        return "claude";
    }

    @Override
    public int priority() {
        return 4;
    }

    @Override
    public String execute(OcrRequest request) throws ProviderException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("max_tokens", 256);
            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            JSONArray content = new JSONArray();
            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image");
            JSONObject source = new JSONObject();
            source.put("type", "base64");
            source.put("media_type", "image/png");
            source.put("data", request.getImageBase64());
            imagePart.put("source", source);
            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", request.getPrompt());
            content.add(imagePart);
            content.add(textPart);
            userMessage.put("content", content);
            messages.add(userMessage);
            body.put("messages", messages);

            String res = restTemplate.postForObject(apiUrl, new HttpEntity<>(body, headers), String.class);
            if (StringUtils.isBlank(res)) {
                return "";
            }
            JSONObject json = JSONObject.parseObject(res);
            JSONArray output = json.getJSONArray("content");
            if (output != null && !output.isEmpty()) {
                for (int i = 0; i < output.size(); i++) {
                    JSONObject item = output.getJSONObject(i);
                    if (item != null && "text".equals(item.getString("type"))) {
                        String text = item.getString("text");
                        if (StringUtils.isNotBlank(text)) {
                            return text;
                        }
                    }
                }
            }
            return "";
        } catch (Exception e) {
            throw new ProviderException("Claude OCR识别失败", e);
        }
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.isNotBlank(apiKey);
    }
}
