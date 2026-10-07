package cn.gdeiassistant.core.privacy.controller;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.privacy.converter.PrivacyConverter;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.privacy.service.PrivacyService;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.*;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PrivacyHttpFlowTest {
    private final PrivacyMapper database=mock(PrivacyMapper.class);
    private final GradeDao grades=mock(GradeDao.class);
    private final ScheduleDao schedules=mock(ScheduleDao.class);
    private MockMvc http;
    @BeforeEach void setup() {
        var certificate=mock(UserCertificateService.class);when(certificate.getUserLoginCertificate("synthetic-session")).thenReturn(new User("synthetic-owner"));
        var service=new PrivacyService();ReflectionTestUtils.setField(service,"privacyMapper",database);ReflectionTestUtils.setField(service,"userCertificateService",certificate);
        ReflectionTestUtils.setField(service,"privacyConverter",Mappers.getMapper(PrivacyConverter.class));ReflectionTestUtils.setField(service,"gradeDao",grades);ReflectionTestUtils.setField(service,"scheduleDao",schedules);
        var controller=new PrivacyController();ReflectionTestUtils.setField(controller,"privacyService",service);
        http=MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalRestExceptionHandler()).build();
    }
    @Test void booleanDefaultsClosePrivacyAndSaveExplicitChoices() throws Exception {
        http.perform(post("/api/privacy").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"facultyOpen\":true,\"majorOpen\":true,\"locationOpen\":true,\"hometownOpen\":true,\"introductionOpen\":true,\"enrollmentOpen\":true,\"ageOpen\":true,\"cacheAllow\":true,\"robotsIndexAllow\":true}"))
                .andExpect(jsonPath("$.success").value(true));
        verify(database).updateFaculty(true,"synthetic-owner");verify(database).updateMajor(true,"synthetic-owner");verify(database).updateLocation(true,"synthetic-owner");
        verify(database).updateHometown(true,"synthetic-owner");verify(database).updateIntroduction(true,"synthetic-owner");verify(database).updateEnrollment(true,"synthetic-owner");
        verify(database).updateAge(true,"synthetic-owner");verify(database).updateCache(true,"synthetic-owner");verify(database).updateRobotsIndex(true,"synthetic-owner");
        verify(grades).removeGrade("synthetic-owner");verify(schedules).removeSchedule("synthetic-owner");
        http.perform(post("/api/privacy").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{}"))
                .andExpect(jsonPath("$.success").value(true));
        verify(database).updateFaculty(false,"synthetic-owner");verify(database).updateCache(false,"synthetic-owner");
    }
    @Test void readUsesRealConversionAndMissingRecordHasAControlledFailure() throws Exception {
        var entity=new PrivacyEntity();entity.setUsername("synthetic-owner");entity.setFacultyOpen(true);entity.setIntroductionOpen(false);entity.setDmPolicy("MUTUAL");
        when(database.selectPrivacy("synthetic-owner")).thenReturn(entity);
        http.perform(get("/api/privacy").requestAttr("sessionId","synthetic-session"))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.facultyOpen").value(true))
                .andExpect(jsonPath("$.data.introductionOpen").value(false));
        when(database.selectPrivacy("synthetic-owner")).thenReturn(null);
        http.perform(get("/api/privacy").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(false));
    }
    @Test void cacheFailurePreservesAllSqlChoicesAndReportsPartialSuccess() throws Exception {
        doThrow(new IllegalStateException("synthetic cache outage")).when(grades).removeGrade("synthetic-owner");
        http.perform(post("/api/privacy").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"cacheAllow\":false,\"robotsIndexAllow\":true}"))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.code").value(206));
        verify(database).updateCache(false,"synthetic-owner");verify(database).updateRobotsIndex(true,"synthetic-owner");
    }
}
