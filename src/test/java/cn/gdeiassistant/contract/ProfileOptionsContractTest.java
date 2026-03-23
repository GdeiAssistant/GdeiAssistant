package cn.gdeiassistant.contract;

import cn.gdeiassistant.core.userProfile.controller.ProfileController;
import cn.gdeiassistant.core.userProfile.service.ProfileOptionsFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileOptionsContractTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProfileController controller = new ProfileController();
        ReflectionTestUtils.setField(controller, "profileOptionsFacade", new ProfileOptionsFacade());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProfileOptionsReturnsCanonicalDictionaryPayload() throws Exception {
        mockMvc.perform(get("/api/profile/options"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/profile-options.success.json")
                ));
    }
}
