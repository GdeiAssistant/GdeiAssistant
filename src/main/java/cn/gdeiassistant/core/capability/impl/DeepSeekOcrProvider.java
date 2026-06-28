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
@Order(0)
public class DeepSeekOcrProvider implements ServiceProvider<OcrRequest, String> {

    @Autowired
    private RestTemplate restTemplate;

    private String apiKey;
    private String model;
    private String baseUrl;

    @Value("${api.ai.ocr.deepseek.api-key:}")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Value("${api.ai.ocr.deepseek.model:deepseek-chat}")
    public void setModel(String model) {
        this.model = model;
    }

    @Value("${api.ai.ocr.deepseek.base-url:https://api.deepseek.com/v1}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String providerName() {
        return "deepseek";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String execute(OcrRequest request) throws ProviderException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            JSONObject body = new JSONObject();
            body.put("model", model);
            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            JSONArray content = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", request.getPrompt());
            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:image/png;base64," + request.getImageBase64());
            imagePart.put("image_url", imageUrl);
            content.add(textPart);
            content.add(imagePart);
            userMessage.put("content", content);
            messages.add(userMessage);
            body.put("messages", messages);

            String url = baseUrl.endsWith("/")
                    ? baseUrl + "chat/completions"
                    : baseUrl + "/chat/completions";
            String res = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            if (StringUtils.isBlank(res)) {
                return "";
            }
            JSONObject json = JSONObject.parseObject(res);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                if (message != null) {
                    String text = message.getString("content");
                    if (StringUtils.isNotBlank(text)) {
                        return text;
                    }
                    JSONArray contentRes = message.getJSONArray("content");
                    if (contentRes != null && !contentRes.isEmpty()) {
                        for (int i = 0; i < contentRes.size(); i++) {
                            JSONObject item = contentRes.getJSONObject(i);
                            if (item != null && "text".equals(item.getString("type"))) {
                                String value = item.getString("text");
                                if (StringUtils.isNotBlank(value)) {
                                    return value;
                                }
                            }
                        }
                    }
                }
            }
            return "";
        } catch (Exception e) {
            throw new ProviderException("DeepSeek OCR识别失败", e);
        }
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.isNotBlank(apiKey);
    }
}
