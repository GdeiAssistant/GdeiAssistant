package cn.gdeiassistant.common.requestwrapper;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XSSRequestWrapperTest {

    @Test
    void doesNotAlterHeadersOrQueryStringWhileStillEscapingFormParameters() {
        MockHttpServletRequest raw = new MockHttpServletRequest();
        raw.addHeader("Authorization", "Bearer token&scope=<read>");
        raw.addHeader("Accept", "application/json, text/plain");
        raw.setQueryString("redirect=https://example.com?a=1&b=2");
        String rawInput = "<a>" + "&raw";
        raw.setParameter("comment", "<script>alert(1)</script>");
        raw.setParameter("password", rawInput);

        XSSRequestWrapper wrapper = new XSSRequestWrapper(raw);
        assertEquals("Bearer token&scope=<read>", wrapper.getHeader("Authorization"));
        assertEquals("redirect=https://example.com?a=1&b=2", wrapper.getQueryString());
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", wrapper.getParameter("comment"));
        assertEquals(rawInput, wrapper.getParameter("password"));
    }
}
