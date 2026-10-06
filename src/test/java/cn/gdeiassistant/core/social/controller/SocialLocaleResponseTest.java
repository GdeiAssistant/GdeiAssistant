package cn.gdeiassistant.core.social.controller;

import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.service.SocialRelationService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SocialLocaleResponseTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "zh-CN|用户不存在|请求参数不合法",
            "zh-HK|搵唔到呢個用戶|提交嘅資料唔啱",
            "zh-TW|找不到使用者|請求參數不合法",
            "en|User not found|Invalid request parameters",
            "ja|ユーザーが見つかりません|リクエストパラメータが無効です",
            "ko|사용자를 찾을 수 없습니다|요청 파라미터가 올바르지 않습니다"
    })
    void socialErrorsAreLocalizedAndMalformedInputNeverEchoesRawExceptions(String locale, String user, String invalid) throws Exception {
        SocialRelationService relations = mock(SocialRelationService.class);
        SocialController controller = new SocialController();
        ReflectionTestUtils.setField(controller, "relationService", relations);
        when(relations.getMe("demo-session")).thenThrow(SocialException.userNotFound());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(get("/api/social/me").requestAttr("sessionId", "demo-session").header("Accept-Language", locale))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(user));
        mvc.perform(get("/api/social/users").param("limit", "private-input-demo")
                        .requestAttr("sessionId", "demo-session").header("Accept-Language", locale))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(invalid));
        verify(relations).getMe("demo-session");
        verifyNoMoreInteractions(relations);
    }
}
