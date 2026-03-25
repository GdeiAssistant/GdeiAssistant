package cn.gdeiassistant.contract;

import cn.gdeiassistant.core.userProfile.controller.ProfileController;
import cn.gdeiassistant.core.userProfile.service.ProfileLocalizationService;
import cn.gdeiassistant.core.userProfile.service.ProfileOptionsFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileOptionsContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProfileController controller = new ProfileController();
        ProfileOptionsFacade facade = new ProfileOptionsFacade();
        ProfileLocalizationService localizationService = new ProfileLocalizationService();
        ReflectionTestUtils.setField(facade, "profileLocalizationService", localizationService);
        ReflectionTestUtils.setField(controller, "profileOptionsFacade", facade);
        ReflectionTestUtils.setField(controller, "profileLocalizationService", localizationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProfileOptionsReturnsStructuredMajorPayload() throws Exception {
        mockMvc.perform(get("/api/profile/options").header("Accept-Language", "en-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.faculties[0].code").value(0))
                .andExpect(jsonPath("$.data.faculties[0].majors[0].code").value("unselected"))
                .andExpect(jsonPath("$.data.faculties[11].majors[1].code").value("software_engineering"))
                .andExpect(jsonPath("$.data.faculties[11].majors[1].label").isNotEmpty())
                .andExpect(jsonPath("$.data.marketplaceItemTypes[0].label").isNotEmpty());
    }
}
