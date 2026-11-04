package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.profile.service.ProfilePatchService;
import cn.gdeiassistant.core.userprofile.controller.ProfilePatchController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises profile patch binding through the MVC message converter actually used
 * by the application, so Jackson-version mismatches cannot silently disable the
 * strict deserializers.
 */
@ExtendWith(MockitoExtension.class)
class ProfilePatchBindingContractTest {

    @Mock
    private ProfilePatchService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProfilePatchController(service))
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void fractionalEnrollmentIsRejectedBeforeService() throws Exception {
        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enrollment\":2024.9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(service);
    }

    @Test
    void numericEnrollmentStringIsRejectedBeforeService() throws Exception {
        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enrollment\":\"2024\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(service);
    }

    @Test
    void nonStringNicknameIsRejectedBeforeService() throws Exception {
        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":123}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(service);
    }

    @Test
    void numericLocationCodeIsRejectedBeforeService() throws Exception {
        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":{\"region\":\"CN\",\"state\":44}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(service);
    }

    @Test
    void validPayloadReachesService() throws Exception {
        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"new\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).save(isNull(), eq(Map.of("nickname", "new")));
    }
}
