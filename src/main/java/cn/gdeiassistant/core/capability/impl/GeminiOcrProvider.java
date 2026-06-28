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
@Order(2)
public class GeminiOcrProvider implements ServiceProvider<OcrRequest, String> {

    @Autowired
    private RestTemplate restTemplate;

    private String apiKey;
    private String model;

    @Value("${api.ai.ocr.gemini.api-key:}")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Value("${api.ai.ocr.gemini.model:gemini-2.0-flash}")
    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String providerName() {
        return "gemini";
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public String execute(OcrRequest request) throws ProviderException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject item = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", request.getPrompt());
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", "image/png");
            inlineData.put("data", request.getImageBase64());
            imagePart.put("inlineData", inlineData);
            parts.add(textPart);
            parts.add(imagePart);
            item.put("parts", parts);
            contents.add(item);
            body.put("contents", contents);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model
                    + ":generateContent?key=" + apiKey;
            String res = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            if (StringUtils.isBlank(res)) {
                return "";
            }
            JSONObject json = JSONObject.parseObject(res);
            JSONArray candidates = json.getJSONArray("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                if (content != null) {
                    JSONArray partsRes = content.getJSONArray("parts");
                    if (partsRes != null && !partsRes.isEmpty()) {
                        String text = partsRes.getJSONObject(0).getString("text");
                        if (StringUtils.isNotBlank(text)) {
                            return text;
                        }
                    }
                }
            }
            return "";
        } catch (Exception e) {
            throw new ProviderException("Gemini OCR识别失败", e);
        }
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.isNotBlank(apiKey);
    }
}
