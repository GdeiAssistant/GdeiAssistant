package cn.gdeiassistant.core.userprofile.controller;

import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.core.profile.service.ProfilePatchService;
import cn.gdeiassistant.core.userprofile.controller.request.ProfilePatchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfilePatchControllerTest {

    @Mock
    private ProfilePatchService service;

    private ProfilePatchController controller;
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @BeforeEach
    void setUp() {
        controller = new ProfilePatchController(service);
        request.setAttribute("sessionId", "synthetic-session");
    }

    @Test
    void forwardsOnlyProvidedFields() throws Exception {
        ProfilePatchRequest body = new ProfilePatchRequest();
        body.setNickname("合成昵称");

        ResponseEntity<? extends JsonResult> response = controller.save(request, body);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() != null && response.getBody().isSuccess());
        verify(service).save("synthetic-session", Map.of("nickname", "合成昵称"));
    }

    @Test
    void nullBodyIsReportedAsInvalidPatch() throws Exception {
        doThrow(new ProfilePatchService.InvalidPatch(Map.of("profile", "请求参数不合法")))
                .when(service).save("synthetic-session", null);

        ResponseEntity<? extends JsonResult> response = controller.save(request, null);

        assertEquals(400, response.getStatusCode().value());
        assertInstanceOf(ProfilePatchController.ValidationResult.class, response.getBody());
        assertEquals("请求参数不合法",
                ((ProfilePatchController.ValidationResult) response.getBody()).getErrors().get("profile"));
    }
}
