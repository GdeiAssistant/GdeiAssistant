package cn.gdeiassistant.common.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RequestCorrelationFilterTest {
    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();
    @AfterEach void cleanup() { MDC.clear(); }

    @Test void safeCallerIdIsReturnedAndContextRestored() throws Exception {
        var request = new MockHttpServletRequest(); var response = new MockHttpServletResponse();
        request.addHeader("X-Request-ID", "synthetic-123.abc"); MDC.put("requestId", "outer-id");
        filter.doFilter(request, response, (req,res) -> {
            assertEquals("synthetic-123.abc", MDC.get("requestId"));
            assertEquals(MDC.get("requestId"), req.getAttribute("requestId"));
        });
        assertEquals("synthetic-123.abc", response.getHeader("X-Request-ID"));
        assertEquals("outer-id", MDC.get("requestId"));
        assertNotNull(request.getAttribute("requestElapsedMs"));
    }
    @Test void missingOversizedAndInjectedIdsAreReplaced() throws Exception {
        for (String supplied : new String[]{"", "x".repeat(65), "line\nforged", "name@example.invalid"}) {
            var request = new MockHttpServletRequest(); var response = new MockHttpServletResponse();
            request.addHeader("X-Request-ID", supplied);
            filter.doFilter(request, response, (req,res) -> assertNotEquals(supplied, MDC.get("requestId")));
            assertNotNull(UUID.fromString(response.getHeader("X-Request-ID")));
            assertNull(MDC.get("requestId"));
        }
        var request = new MockHttpServletRequest(); var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req,res) -> assertNotNull(MDC.get("requestId")));
        assertNotNull(UUID.fromString(response.getHeader("X-Request-ID")));
    }
    @Test void failingChainDoesNotLeaveContextBehind() {
        var request = new MockHttpServletRequest(); var response = new MockHttpServletResponse();
        var failure = new IllegalStateException("synthetic");
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> filter.doFilter(request, response, (req,res) -> {throw failure;})));
        assertNull(MDC.get("requestId"));
        assertNotNull(request.getAttribute("requestElapsedMs"));
    }
}
