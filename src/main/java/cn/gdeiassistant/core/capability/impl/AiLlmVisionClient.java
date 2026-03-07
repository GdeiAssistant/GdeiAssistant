package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AiLlmVisionClient {

    @Autowired
    private RestTemplate restTemplate;

    private String provider;

    private String openaiApiKey;
    private String openaiModel;

    private String geminiApiKey;
    private String geminiModel;

    private String doubaoApiKey;
    private String doubaoModel;
    private String doubaoBaseUrl;

    @Value("${api.ai.ocr.provider:openai}")
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Value("${api.ai.ocr.openai.api-key:}")
    public void setOpenaiApiKey(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey;
    }

    @Value("${api.ai.ocr.openai.model:gpt-4.1-mini}")
    public void setOpenaiModel(String openaiModel) {
        this.openaiModel = openaiModel;
    }

    @Value("${api.ai.ocr.gemini.api-key:}")
    public void setGeminiApiKey(String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
    }

    @Value("${api.ai.ocr.gemini.model:gemini-2.0-flash}")
    public void setGeminiModel(String geminiModel) {
        this.geminiModel = geminiModel;
    }

    @Value("${api.ai.ocr.doubao.api-key:}")
    public void setDoubaoApiKey(String doubaoApiKey) {
        this.doubaoApiKey = doubaoApiKey;
    }

    @Value("${api.ai.ocr.doubao.model:doubao-1.5-vision-pro-32k}")
    public void setDoubaoModel(String doubaoModel) {
        this.doubaoModel = doubaoModel;
    }

    @Value("${api.ai.ocr.doubao.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    public void setDoubaoBaseUrl(String doubaoBaseUrl) {
        this.doubaoBaseUrl = doubaoBaseUrl;
    }

    public String recognizeCaptcha(String imageBase64, String typeHint, int length) throws RecognitionException {
        String prompt = "You are a captcha OCR engine. Read the captcha image and return ONLY the captcha text without spaces or punctuation."
                + " Type hint: " + typeHint + "."
                + (length > 0 ? " Expected length: " + length + "." : "")
                + " Output plain text only.";
        String text = invokeProvider(imageBase64, prompt);
        if (StringUtils.isBlank(text)) {
            throw new RecognitionException("AI验证码识别失败");
        }
        return text;
    }

    public String recognizeDigits(String imageBase64) throws RecognitionException {
        String prompt = "Extract only digits from this image. Return digits only, no other text.";
        String text = invokeProvider(imageBase64, prompt);
        if (StringUtils.isBlank(text)) {
            throw new RecognitionException("AI数字识别失败");
        }
        return text;
    }

    private String invokeProvider(String imageBase64, String prompt) throws RecognitionException {
        String selected = StringUtils.isNotBlank(provider) ? provider.trim().toLowerCase() : "openai";
        switch (selected) {
            case "openai":
                return callOpenAi(imageBase64, prompt);
            case "gemini":
                return callGemini(imageBase64, prompt);
            case "doubao":
                return callDoubao(imageBase64, prompt);
            default:
                throw new RecognitionException("AI识别服务配置不合法：api.ai.ocr.provider 仅支持 openai / gemini / doubao");
        }
    }

    private String callOpenAi(String imageBase64, String prompt) throws RecognitionException {
        if (StringUtils.isBlank(openaiApiKey)) {
            throw new RecognitionException("AI识别未启用：请配置 api.ai.ocr.openai.api-key");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            JSONObject body = new JSONObject();
            body.put("model", openaiModel);
            JSONArray input = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            JSONArray content = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("type", "input_text");
            textPart.put("text", prompt);
            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "input_image");
            imagePart.put("image_url", "data:image/png;base64," + imageBase64);
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
                        return outContent.getJSONObject(0).getString("text");
                    }
                }
            }
            return "";
        } catch (Exception e) {
            throw new RecognitionException("OpenAI 识别失败");
        }
    }

    private String callGemini(String imageBase64, String prompt) throws RecognitionException {
        if (StringUtils.isBlank(geminiApiKey)) {
            throw new RecognitionException("AI识别未启用：请配置 api.ai.ocr.gemini.api-key");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject item = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", "image/png");
            inlineData.put("data", imageBase64);
            imagePart.put("inlineData", inlineData);
            parts.add(textPart);
            parts.add(imagePart);
            item.put("parts", parts);
            contents.add(item);
            body.put("contents", contents);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel
                    + ":generateContent?key=" + geminiApiKey;
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
                        return partsRes.getJSONObject(0).getString("text");
                    }
                }
            }
            return "";
        } catch (Exception e) {
            throw new RecognitionException("Gemini 识别失败");
        }
    }

    private String callDoubao(String imageBase64, String prompt) throws RecognitionException {
        if (StringUtils.isBlank(doubaoApiKey)) {
            throw new RecognitionException("AI识别未启用：请配置 api.ai.ocr.doubao.api-key");
        }
        if (StringUtils.isBlank(doubaoBaseUrl)) {
            throw new RecognitionException("AI识别未启用：请配置 api.ai.ocr.doubao.base-url");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(doubaoApiKey);

            JSONObject body = new JSONObject();
            body.put("model", doubaoModel);
            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            JSONArray content = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", prompt);
            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:image/png;base64," + imageBase64);
            imagePart.put("image_url", imageUrl);
            content.add(textPart);
            content.add(imagePart);
            userMessage.put("content", content);
            messages.add(userMessage);
            body.put("messages", messages);

            String url = doubaoBaseUrl.endsWith("/")
                    ? doubaoBaseUrl + "chat/completions"
                    : doubaoBaseUrl + "/chat/completions";
            String res = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
            if (StringUtils.isBlank(res)) {
                return "";
            }
            JSONObject json = JSONObject.parseObject(res);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                if (message != null) {
                    return message.getString("content");
                }
            }
            return "";
        } catch (Exception e) {
            throw new RecognitionException("豆包识别失败");
        }
    }
}
