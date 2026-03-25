package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.pojo.Entity.Introduction;
import cn.gdeiassistant.core.iPAddress.service.IPAddressService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userProfile.controller.ProfileController;
import cn.gdeiassistant.core.userProfile.controller.mapper.ProfileResponseMapper;
import cn.gdeiassistant.core.userProfile.service.ProfileLocalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserProfileContractTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private IPAddressService ipAddressService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        ProfileLocalizationService localizationService = new ProfileLocalizationService();
        ProfileResponseMapper mapper = new ProfileResponseMapper();
        ReflectionTestUtils.setField(mapper, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(mapper, "ipAddressService", ipAddressService);
        ReflectionTestUtils.setField(mapper, "profileLocalizationService", localizationService);

        ProfileController controller = new ProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(controller, "profileResponseMapper", mapper);
        ReflectionTestUtils.setField(controller, "profileLocalizationService", localizationService);

        ProfileVO profile = new ProfileVO();
        profile.setUsername("20231234");
        profile.setNickname("测试同学");
        profile.setFaculty(11);
        profile.setMajor("software_engineering");
        profile.setEnrollment(2023);
        profile.setLocationRegion("CN");
        profile.setLocationState("44");
        profile.setLocationCity("1");
        profile.setHometownRegion("CN");
        profile.setHometownState("44");
        profile.setHometownCity("5");

        Introduction introduction = new Introduction();
        introduction.setIntroductionContent("喜欢图书馆和校园摄影。");

        when(userProfileService.getSelfUserProfile("test-session")).thenReturn(profile);
        when(userProfileService.getSelfUserAvatar("test-session")).thenReturn("https://cdn.gdeiassistant.cn/avatar/test-user.png");
        when(userProfileService.getSelfUserIntroduction("test-session")).thenReturn(introduction);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCurrentUserProfile_returnsStructuredFields() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .requestAttr("sessionId", "test-session")
                        .header("Accept-Language", "en-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("20231234"))
                .andExpect(jsonPath("$.data.faculty.code").value(11))
                .andExpect(jsonPath("$.data.faculty.label").isNotEmpty())
                .andExpect(jsonPath("$.data.major.code").value("software_engineering"))
                .andExpect(jsonPath("$.data.major.label").isNotEmpty())
                .andExpect(jsonPath("$.data.location.region").value("CN"))
                .andExpect(jsonPath("$.data.location.state").value("44"))
                .andExpect(jsonPath("$.data.location.city").value("1"))
                .andExpect(jsonPath("$.data.location.displayName").isNotEmpty())
                .andExpect(jsonPath("$.data.hometown.region").value("CN"))
                .andExpect(jsonPath("$.data.hometown.displayName").isNotEmpty());
    }
}
