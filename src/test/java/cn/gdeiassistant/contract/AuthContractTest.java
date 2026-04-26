package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.core.userLogin.controller.AuthController;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import cn.gdeiassistant.common.tools.Utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for auth endpoints.
 * Verifies token issuance shape and error response format — used by
 * WeChat Mini Program and other clients to validate session lifecycle.
 */
class AuthContractTest {

    private MockMvc mockMvc;
    private UserLoginService userLoginService;
    private UserCertificateService userCertificateService;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userLoginService = mock(UserLoginService.class);
        userCertificateService = mock(UserCertificateService.class);
        jwtUtil = mock(JwtUtil.class);

        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "userLoginService", userLoginService);
        ReflectionTestUtils.setField(controller, "userCertificateService", userCertificateService);
        ReflectionTestUtils.setField(controller, "jwtUtil", jwtUtil);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void loginSuccessReturnsTokenInData() throws Exception {
        when(jwtUtil.createToken(anyString(), anyString())).thenReturn("jwt-token-example");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"testpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token-example"));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        doThrow(new PasswordIncorrectException("账号或密码错误"))
                .when(userLoginService).userLogin(anyString(), anyString(), anyString(), anyBoolean());

        mockMvc.perform(post("/api/auth/login")
                        .header("Accept-Language", "en-US")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").value("Incorrect username or password"));
    }

    @Test
    void loginMissingUsernameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"testpass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginMissingPasswordReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutReturnsSuccess() throws Exception {
        // Logout is always successful regardless of session state.
        // Testing with no session attribute (null path) since static HTTP client
        // cleanup is a side-effect not testable in standalone MockMvc.
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void validateReturnsSuccessShape() throws Exception {
        // In production, ApiAuthInterceptor returns 401 before this method is
        // reached when the JWT is invalid. Standalone MockMvc does not register
        // interceptors, so this test validates the controller response shape only.
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }
}
