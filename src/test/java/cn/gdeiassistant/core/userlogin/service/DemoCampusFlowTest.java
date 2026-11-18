package cn.gdeiassistant.core.userlogin.service;

import cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException;
import cn.gdeiassistant.common.pojo.entity.Grade;
import cn.gdeiassistant.common.pojo.entity.Schedule;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.usercertificate.UserCertificateDao;
import cn.gdeiassistant.core.cardquery.pojo.CardQuery;
import cn.gdeiassistant.core.cardquery.service.CardQueryService;
import cn.gdeiassistant.core.demo.DemoAccount;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.gradequery.service.GradeService;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.schedulequery.service.ScheduleService;
import cn.gdeiassistant.integration.edu.EduSystemClient;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DemoCampusFlowTest {

    private final UserCertificateDao sessions = mock(UserCertificateDao.class);
    private final HttpClientUtils httpClients = mock(HttpClientUtils.class);
    private final UserCertificateService certificates = new UserCertificateService(
            null, httpClients, sessions, null, null, null);

    @BeforeEach
    void configureDemoSession() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("staging");
        certificates.configureDemoAccount(new DemoAccount(environment, true, "demo-student", "demo-token-value"));
        when(sessions.queryUserLoginCertificate("demo-session")).thenReturn(new User("demo-student"));
    }

    @Test
    void gradesAndScheduleRemainAvailableAfterRefreshWithoutCampusAccess() throws Exception {
        GradeDao grades = mock(GradeDao.class);
        ScheduleDao schedules = mock(ScheduleDao.class);
        EduSystemClient school = mock(EduSystemClient.class);
        GradeService gradeService = new GradeService(certificates, grades, school);
        ScheduleService scheduleService = new ScheduleService(null, certificates, schedules, school);

        gradeService.updateGradeCache("demo-session");
        scheduleService.updateScheduleCache("demo-session");

        var grade = gradeService.queryGrade("demo-session", 0);
        assertThat(grade.getFirstTermGradeList()).extracting(Grade::getGradeId).containsExactly("DEMO-101");
        assertThat(grade.getSecondTermGradeList()).extracting(Grade::getGradeId).containsExactly("DEMO-102");
        assertThat(gradeService.queryGrade("demo-session", 1).getFirstTermGradeList()).isEmpty();
        var schedule = scheduleService.querySchedule("demo-session", 20);
        assertThat(schedule.getWeek()).isEqualTo(20);
        assertThat(schedule.getScheduleList()).extracting(Schedule::getId).containsExactly("DEMO-101", "DEMO-102");
        verifyNoInteractions(grades, school, httpClients);
        verify(schedules).queryCustomSchedule("demo-student");
        verifyNoMoreInteractions(schedules);
    }

    @Test
    void demoCardReadsWorkAndCardLossCannotReachTheCampusSystem() throws Exception {
        CardQueryService cardService = new CardQueryService(httpClients, null, certificates, null);
        CardQuery date = new CardQuery();
        date.setYear(2026);
        date.setMonth(11);
        date.setDate(16);

        assertThat(cardService.cardInfoQuery("demo-session").getCardBalance()).isEqualTo("84.50");
        var card = cardService.cardQuery("demo-session", date);
        assertThat(card.getCardList()).hasSize(2).allSatisfy(transaction ->
                assertThat(transaction.getTradeTime()).startsWith("2026-11-16"));
        assertThrows(FeatureNotEnabledException.class, () -> cardService.cardLost("demo-session", "123456"));
        verifyNoInteractions(httpClients);
    }
}
