package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.campuscredential.controller.CampusCredentialController;
import cn.gdeiassistant.core.campuscredential.pojo.vo.CampusCredentialStatusVO;
import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Date;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CampusCredentialContractTest {

    private MockMvc mockMvc;
    private CampusCredentialService campusCredentialService;

    @BeforeEach
    void setUp() throws Exception {
        campusCredentialService = Mockito.mock(CampusCredentialService.class);

        CampusCredentialController controller = new CampusCredentialController();
        ReflectionTestUtils.setField(controller, "campusCredentialService", campusCredentialService);

        CampusCredentialStatusVO statusVO = new CampusCredentialStatusVO();
        statusVO.setHasActiveConsent(true);
        statusVO.setHasSavedCredential(true);
        statusVO.setQuickAuthEnabled(true);
        statusVO.setConsentedAt(Date.valueOf("2026-05-11"));
        statusVO.setPolicyDate(Date.valueOf("2026-04-25"));
        statusVO.setEffectiveDate(Date.valueOf("2026-05-11"));
        statusVO.setMaskedCampusAccount("camp****2026");

        when(campusCredentialService.getSelfStatus(anyString())).thenReturn(statusVO);
        when(campusCredentialService.updateQuickAuth(anyString(), anyBoolean())).thenReturn(statusVO);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void getStatusReturnsOnlySafeMetadata() throws Exception {
        mockMvc.perform(get("/api/campus-credential/status")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasActiveConsent").value(true))
                .andExpect(jsonPath("$.data.hasSavedCredential").value(true))
                .andExpect(jsonPath("$.data.quickAuthEnabled").value(true))
                .andExpect(jsonPath("$.data.maskedCampusAccount").value("camp****2026"))
                .andExpect(content().string(not(containsString("campususer2026"))))
                .andExpect(content().string(not(containsString("\"password\""))))
                .andExpect(content().string(not(containsString("\"token\""))))
                .andExpect(content().string(not(containsString("\"session\""))))
                .andExpect(content().string(not(containsString("\"captcha\""))));
    }

    @Test
    void updateQuickAuthReturnsStatusPayload() throws Exception {
        mockMvc.perform(post("/api/campus-credential/quick-auth")
                        .requestAttr("sessionId", "test-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quickAuthEnabled").value(true));
    }
}
