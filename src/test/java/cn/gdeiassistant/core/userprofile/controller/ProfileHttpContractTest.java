package cn.gdeiassistant.core.userprofile.controller;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.pojo.entity.Introduction;
import cn.gdeiassistant.common.tools.utils.LocationUtils;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.ipaddress.service.IPAddressService;
import cn.gdeiassistant.core.userprofile.controller.mapper.ProfileResponseMapper;
import cn.gdeiassistant.core.userprofile.controller.support.ProfileLocationValidator;
import cn.gdeiassistant.core.userprofile.service.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.Date;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProfileHttpContractTest {
    private final UserProfileService service=mock(UserProfileService.class);
    private final IPAddressService ip=mock(IPAddressService.class);
    private MockMvc http;
    private java.util.Map<Integer,String> originalFaculty;
    @BeforeEach void setup() {
        originalFaculty=UserProfileService.getFacultyMap();
        var faculties=new java.util.HashMap<Integer,String>();
        var options=cn.gdeiassistant.common.constant.OptionConstantUtils.FACULTY_OPTIONS;
        for(int i=0;i<options.length;i++)faculties.put(i,options[i]);
        new UserProfileService().setFacultyMap(faculties);
        var localization=new ProfileLocalizationService();var mapper=new ProfileResponseMapper();
        ReflectionTestUtils.setField(mapper,"userProfileService",service);ReflectionTestUtils.setField(mapper,"ipAddressService",ip);
        ReflectionTestUtils.setField(mapper,"profileLocalizationService",localization);
        var controller=new ProfileController();ReflectionTestUtils.setField(controller,"userProfileService",service);
        ReflectionTestUtils.setField(controller,"profileResponseMapper",mapper);ReflectionTestUtils.setField(controller,"profileLocalizationService",localization);
        ReflectionTestUtils.setField(controller,"profileLocationValidator",new ProfileLocationValidator());
        ReflectionTestUtils.setField(controller,"profileOptionsFacade",mock(ProfileOptionsFacade.class));
        http=MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalRestExceptionHandler()).build();
    }
    @AfterEach void restoreFaculty(){new UserProfileService().setFacultyMap(originalFaculty);}
    private void update(String path,String body,boolean success) throws Exception {
        http.perform(post(path).requestAttr("sessionId","synthetic-session").contentType("application/json").content(body)).andExpect(jsonPath("$.success").value(success));
    }
    @Test void readProfileAssemblesPublicResponseAndToleratesUnavailableIpLookup() throws Exception {
        var profile=new ProfileVO();profile.setUsername("synthetic-owner");profile.setNickname("合成昵称");profile.setEnrollment(2024);profile.setFaculty(1);profile.setBirthday(new Date(946684800000L));
        profile.setLocationRegion("CN");profile.setLocationState("44");profile.setLocationCity("1");profile.setHometownRegion("HK");
        when(service.getSelfUserProfile("synthetic-session")).thenReturn(profile);
        when(service.getSelfUserAvatar("synthetic-session")).thenReturn("/api/profile/avatar/content");
        var intro=new Introduction();intro.setIntroductionContent("合成简介");when(service.getSelfUserIntroduction("synthetic-session")).thenReturn(intro);
        when(ip.getSelfUserLatestPostTypeIPAddress("synthetic-session")).thenThrow(new IllegalStateException("synthetic outage"));
        http.perform(get("/api/user/profile").requestAttr("sessionId","synthetic-session").header("Accept-Language","en-US"))
                .andExpect(jsonPath("$.code").value(200)).andExpect(jsonPath("$.data.nickname").value("合成昵称"))
                .andExpect(jsonPath("$.data.enrollment").value("2024")).andExpect(jsonPath("$.data.introduction").value("合成简介"))
                .andExpect(jsonPath("$.data.avatar").value("/api/profile/avatar/content")).andExpect(jsonPath("$.data.ipArea").value(""))
                .andExpect(jsonPath("$.data.location.regionCode").value("CN"));
        http.perform(get("/api/profile/nickname").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data").value("合成昵称"));
        http.perform(get("/api/introduction").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data").value("合成简介"));
        http.perform(get("/api/profile/avatar").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data").value("/api/profile/avatar/content"));
        when(service.getSelfUserProfile("synthetic-session")).thenReturn(null);
        http.perform(get("/api/user/profile").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.code").value(404));
    }
    @Test void nicknameAndIntroductionEscapeMarkupAndRejectOverlongInput() throws Exception {
        update("/api/profile/nickname","{\"nickname\":\"<b>合成昵称</b>\"}",true);
        verify(service).updateNickname("synthetic-session","&lt;b&gt;合成昵称&lt;/b&gt;");
        update("/api/profile/nickname","{\"nickname\":\"\"}",false);update("/api/profile/nickname","{\"nickname\":\""+"字".repeat(33)+"\"}",false);
        update("/api/introduction","{\"introduction\":\"<script>合成</script>\"}",true);
        verify(service).updateIntroduction("synthetic-session","&lt;script&gt;合成&lt;/script&gt;");
        update("/api/introduction","{\"introduction\":\"\"}",true);verify(service).updateIntroduction("synthetic-session",null);
        update("/api/introduction","{}",false);update("/api/introduction","{\"introduction\":\""+"字".repeat(81)+"\"}",false);
    }
    @Test void birthdayEnrollmentFacultyAndMajorHaveDistinctResetAndUpdateSemantics() throws Exception {
        update("/api/profile/birthday","{}",true);verify(service).resetBirthday("synthetic-session");
        update("/api/profile/birthday","{\"year\":2000,\"month\":2,\"date\":29}",true);verify(service).updateBirthday("synthetic-session",2000,2,29);
        update("/api/profile/birthday","{\"year\":2000}",false);
        update("/api/profile/enrollment","{}",true);verify(service).resetEnrollment("synthetic-session");
        update("/api/profile/enrollment","{\"year\":2024}",true);verify(service).updateEnrollment("synthetic-session",2024);
        update("/api/profile/enrollment","{\"year\":"+(LocalDate.now().getYear()+1)+"}",false);
        update("/api/profile/faculty","{\"faculty\":1}",true);verify(service).updateFaculty("synthetic-session",1);verify(service).updateMajor("synthetic-session",null);
        update("/api/profile/faculty","{\"faculty\":-1}",false);
        var profile=new ProfileVO();profile.setFaculty(1);when(service.getSelfUserProfile("synthetic-session")).thenReturn(profile);
        String major=cn.gdeiassistant.common.constant.OptionConstantUtils.MAJOR_OPTIONS_BY_FACULTY[1][0];
        update("/api/profile/major","{\"major\":\""+major+"\"}",true);verify(service).updateMajor("synthetic-session",major);
        update("/api/profile/major","{\"major\":\"合成不存在专业\"}",false);update("/api/profile/major","{}",false);
    }
    @Test void locationAndHometownUseTheSameDictionaryValidation() throws Exception {
        var region=LocationUtils.getRegionMap().values().stream().filter(r->r.getStateMap()==null||r.getStateMap().isEmpty()).findFirst().orElseThrow().getCode();
        for(String path:new String[]{"/api/profile/location","/api/profile/hometown"}) {
            update(path,"{\"region\":\""+region+"\",\"state\":\"ignored\",\"city\":\"ignored\"}",true);
            update(path,"{\"region\":\"nonexistent\"}",false);update(path,"{}",false);
        }
        verify(service).updateLocation("synthetic-session",region,null,null);verify(service).updateHometown("synthetic-session",region,null,null);
        http.perform(get("/api/profile/locations")).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data").isArray());
        http.perform(get("/api/profile/options")).andExpect(jsonPath("$.success").value(true));
    }
    @Test void avatarUsesEitherACompleteFilePairOrACompleteObjectKeyPair() throws Exception {
        http.perform(multipart("/api/profile/avatar").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(false));
        http.perform(multipart("/api/profile/avatar").param("avatarKey","synthetic/small").param("avatarHdKey","synthetic/large").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));
        verify(service).updateAvatarByObjectKey("synthetic-session","synthetic/small");verify(service).updateHighDefinitionAvatarByObjectKey("synthetic-session","synthetic/large");
        http.perform(multipart("/api/profile/avatar").file(new MockMultipartFile("avatar",new byte[]{1})).file(new MockMultipartFile("avatar_hd",new byte[]{2})).requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));
        verify(service).updateAvatar(eq("synthetic-session"),any());verify(service).updateHighDefinitionAvatar(eq("synthetic-session"),any());
        http.perform(delete("/api/profile/avatar").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));verify(service).deleteAvatar("synthetic-session");
        when(service.getSelfUserAvatar("synthetic-session")).thenReturn(null);
        http.perform(get("/api/profile/avatar").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data").value(""));
    }
}
