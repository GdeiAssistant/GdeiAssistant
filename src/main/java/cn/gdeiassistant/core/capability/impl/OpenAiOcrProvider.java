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
@Order(3)
public class OpenAiOcrProvider implements ServiceProvider<OcrRequest, String> {

    @Autowired
    private RestTemplate restTemplate;

    private String apiKey;
    private String model;

    @Value("${api.ai.ocr.openai.api-key:}")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Value("${api.ai.ocr.openai.model:gpt-4.1-mini}")
    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String providerName() {
        return "openai";
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public String execute(OcrRequest request) throws ProviderException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            JSONObject body = new JSONObject();
            body.put("model", model);
            JSONArray input = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            JSONArray content = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("type", "input_text");
            textPart.put("text", request.getPrompt());
            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "input_image");
            imagePart.put("image_url", "data:image/png;base64," + request.getImageBase64());
            content.add(textPart);
            content.add(imagePart);
            userMessage.put("content", content);
            input.add(userMessage);
            body.put("input", input);

            String res = restTemplate.postForObject("https://api.openai.com/v1/responses", new HttpEntity<>(body, headers), String.class);
            if (StringUtils.isBlank(res)) {
                return "";
            }
            JSONObject json = JSONObject.parseObject(res);
            String out = json.getString("output_text");
            if (StringUtils.isNotBlank(out)) {
                return out;
            }
            JSONArray output = json.getJSONArray("output");
            if (output != null && !output.isEmpty()) {
                Object first = output.get(0);
                if (first instanceof JSONObject) {
                    JSONArray outContent = ((JSONObject) first).getJSONArray("content");
                    if (outContent != null && !outContent.isEmpty()) {
                        String text = outContent.getJSONObject(0).getString("text");
                        if (StringUtils.isNotBlank(text)) {
                            return text;
                        }
                    }
                }
            }
            return "";
        } catch (Exception e) {
            throw new ProviderException("OpenAI OCR识别失败", e);
        }
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.isNotBlank(apiKey);
    }
}
