package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.pojo.Entity.Introduction;
import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.core.i18n.I18nTranslationService;
import cn.gdeiassistant.core.iPAddress.service.IPAddressService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userProfile.controller.ProfileController;
import cn.gdeiassistant.core.userProfile.controller.mapper.ProfileResponseMapper;
import cn.gdeiassistant.core.userProfile.service.ProfileLocalizationService;
import cn.gdeiassistant.core.userProfile.service.ProfileOptionsFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserProfileContractTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private IPAddressService ipAddressService;

    @Mock
    private I18nTranslationService translationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        ProfileLocalizationService localizationService = new ProfileLocalizationService();
        ReflectionTestUtils.setField(localizationService, "translationService", translationService);

        ProfileOptionsFacade profileOptionsFacade = new ProfileOptionsFacade();

        ProfileResponseMapper mapper = new ProfileResponseMapper();
        ReflectionTestUtils.setField(mapper, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(mapper, "ipAddressService", ipAddressService);
        ReflectionTestUtils.setField(mapper, "profileLocalizationService", localizationService);

        ProfileController controller = new ProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(controller, "profileResponseMapper", mapper);
        ReflectionTestUtils.setField(controller, "profileLocalizationService", localizationService);
        ReflectionTestUtils.setField(controller, "profileOptionsFacade", profileOptionsFacade);

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

        IPAddressRecord ipAddressRecord = new IPAddressRecord();
        ipAddressRecord.setArea("广东省");

        lenient().when(userProfileService.getSelfUserProfile("test-session")).thenReturn(profile);
        lenient().when(userProfileService.getSelfUserAvatar("test-session")).thenReturn("https://cdn.gdeiassistant.cn/avatar/test-user.png");
        lenient().when(userProfileService.getSelfUserIntroduction("test-session")).thenReturn(introduction);
        lenient().when(ipAddressService.getSelfUserLatestPostTypeIPAddress("test-session")).thenReturn(ipAddressRecord);
        lenient().when(translationService.translate(anyString(), anyString())).thenAnswer(invocation -> {
            String text = invocation.getArgument(0, String.class);
            String language = invocation.getArgument(1, String.class);
            if ("广东省".equals(text) && "en".equals(language)) {
                return "Guangdong Province";
            }
            return text + "-" + language;
        });

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCurrentUserProfile_returnsCodeOnlyProfileFields() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .requestAttr("sessionId", "test-session")
                        .header("Accept-Language", "en-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("20231234"))
                .andExpect(jsonPath("$.data.facultyCode").value(11))
                .andExpect(jsonPath("$.data.faculty").doesNotExist())
                .andExpect(jsonPath("$.data.majorCode").value("software_engineering"))
                .andExpect(jsonPath("$.data.major").doesNotExist())
                .andExpect(jsonPath("$.data.location.regionCode").value("CN"))
                .andExpect(jsonPath("$.data.location.stateCode").value("44"))
                .andExpect(jsonPath("$.data.location.cityCode").value("1"))
                .andExpect(jsonPath("$.data.location.displayName").doesNotExist())
                .andExpect(jsonPath("$.data.hometown.regionCode").value("CN"))
                .andExpect(jsonPath("$.data.hometown.stateCode").value("44"))
                .andExpect(jsonPath("$.data.hometown.cityCode").value("5"))
                .andExpect(jsonPath("$.data.ipArea").value("Guangdong Province"));
    }

    @Test
    void getProfileOptions_returnsCodeOnlyDictionaries() throws Exception {
        mockMvc.perform(get("/api/profile/options")
                        .header("Accept-Language", "en-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.faculties[0].code").exists())
                .andExpect(jsonPath("$.data.faculties[0].label").doesNotExist())
                .andExpect(jsonPath("$.data.faculties[0].majors[0]").isString())
                .andExpect(jsonPath("$.data.marketplaceItemTypes[0]").isNumber())
                .andExpect(jsonPath("$.data.lostFoundItemTypes[0]").isNumber())
                .andExpect(jsonPath("$.data.lostFoundModes[0]").isNumber());
    }

    @Test
    void getProfileLocations_returnsCodeOnlyTree() throws Exception {
        mockMvc.perform(get("/api/profile/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").exists())
                .andExpect(jsonPath("$.data[0].name").doesNotExist())
                .andExpect(jsonPath("$.data[0].aliasesName").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"children\"")));
    }
}
