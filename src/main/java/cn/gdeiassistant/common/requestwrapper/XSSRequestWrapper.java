package cn.gdeiassistant.common.requestwrapper;

import org.apache.commons.lang3.StringEscapeUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

    private final String contentType;

    public XSSRequestWrapper(HttpServletRequest request) {
        super(request);
        this.contentType = request.getContentType();
    }

    private boolean shouldSkip() {
        return contentType != null && contentType.startsWith("application/json");
    }

    private boolean isPasswordParameter(String name) {
        return name != null && name.toLowerCase().contains("password");
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return value != null ? StringEscapeUtils.escapeHtml4(value) : null;
    }

    @Override
    public String getQueryString() {
        String value = super.getQueryString();
        return value != null ? StringEscapeUtils.escapeHtml4(value) : null;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null || shouldSkip() || isPasswordParameter(name)) {
            return value;
        }
        return StringEscapeUtils.escapeHtml4(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null || shouldSkip() || isPasswordParameter(name)) {
            return values;
        }
        int length = values.length;
        String[] escapseValues = new String[length];
        for(int i = 0; i < length; i++){
            escapseValues[i] = StringEscapeUtils.escapeHtml4(values[i]);
        }
        return escapseValues;
    }
}