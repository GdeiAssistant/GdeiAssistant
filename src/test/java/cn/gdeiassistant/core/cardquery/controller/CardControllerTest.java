package cn.gdeiassistant.core.cardquery.controller;

import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.cardquery.service.CardQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardQueryService cardQueryService;

    @InjectMocks
    private CardController controller;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setAttribute("sessionId", "test-session-id");
    }

    @Test
    void shouldAcceptCardPasswordInRequestBody() throws Exception {
        Map<String, String> body = Map.of("cardPassword", "123456");

        JsonResult result = controller.cardLost(request, body);

        assertTrue(result.isSuccess());
        verify(cardQueryService).cardLost("test-session-id", "123456");
    }

    @Test
    void shouldRejectNullCardPassword() {
        Map<String, String> body = Map.of();

        assertThrows(IllegalArgumentException.class, () -> controller.cardLost(request, body));
    }

    @Test
    void shouldRejectNonNumericCardPassword() {
        Map<String, String> body = Map.of("cardPassword", "abc123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.cardLost(request, body));
        assertTrue(ex.getMessage().contains("cardPassword must be numeric"));
    }

    @Test
    void shouldAcceptEmptyNumericPassword() throws Exception {
        // 空字符串匹配 ^[0-9]*$（* 允许零次匹配）
        Map<String, String> body = Map.of("cardPassword", "");

        JsonResult result = controller.cardLost(request, body);

        assertTrue(result.isSuccess());
        verify(cardQueryService).cardLost("test-session-id", "");
    }

    @Test
    void shouldAcceptAllDigitsPassword() throws Exception {
        Map<String, String> body = Map.of("cardPassword", "000000");

        JsonResult result = controller.cardLost(request, body);

        assertTrue(result.isSuccess());
        verify(cardQueryService).cardLost("test-session-id", "000000");
    }

    @Test
    void shouldRejectPasswordWithSpecialChars() {
        Map<String, String> body = Map.of("cardPassword", "123!@#");

        assertThrows(IllegalArgumentException.class, () -> controller.cardLost(request, body));
    }
}
