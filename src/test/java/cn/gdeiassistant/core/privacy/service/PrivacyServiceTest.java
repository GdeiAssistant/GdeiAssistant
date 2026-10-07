package cn.gdeiassistant.core.privacy.service;

import cn.gdeiassistant.common.exception.commonexception.CacheClearException;
import cn.gdeiassistant.common.exception.databaseexception.UserNotExistException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.privacy.converter.PrivacyConverter;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.privacy.pojo.vo.PrivacyVO;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrivacyServiceTest {
    private final PrivacyMapper mapper = mock(PrivacyMapper.class);
    private final PrivacyConverter converter = mock(PrivacyConverter.class);
    private final GradeDao grades = mock(GradeDao.class);
    private final ScheduleDao schedules = mock(ScheduleDao.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final PrivacyService service = new PrivacyService();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "privacyMapper", mapper);
        ReflectionTestUtils.setField(service, "privacyConverter", converter);
        ReflectionTestUtils.setField(service, "gradeDao", grades);
        ReflectionTestUtils.setField(service, "scheduleDao", schedules);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("owner"));
    }
    @Test void missingUserIsNotAnImplicitPublicPrivacyProfile() throws Exception {
        assertThrows(UserNotExistException.class, () -> service.getOtherUserPrivacySetting("absent"));
        var entity = new PrivacyEntity(); var view = new PrivacyVO();
        when(mapper.selectPrivacy("owner")).thenReturn(entity); when(converter.toVO(entity)).thenReturn(view);
        assertSame(view, service.getSelfUserPrivacySetting("session"));
    }
    @Test void eachSettingTargetsAuthenticatedOwner() throws Exception {
        service.updateFaculty(false, "session"); service.updateMajor(true, "session");
        service.updateLocation(false, "session"); service.updateHometown(true, "session");
        service.updateIntroduction(false, "session"); service.updateEnrollment(true, "session");
        service.updateAge(false, "session"); service.updateRobotsIndex(true, "session");
        verify(mapper).updateFaculty(false, "owner"); verify(mapper).updateMajor(true, "owner");
        verify(mapper).updateLocation(false, "owner"); verify(mapper).updateHometown(true, "owner");
        verify(mapper).updateIntroduction(false, "owner"); verify(mapper).updateEnrollment(true, "owner");
        verify(mapper).updateAge(false, "owner"); verify(mapper).updateRobotsIndex(true, "owner");
    }
    @Test void cacheSettingIsPersistedBeforeBothCachesAreCleared() throws Exception {
        service.updateCache(false, "session");
        var order = inOrder(mapper, grades, schedules);
        order.verify(mapper).updateCache(false, "owner"); order.verify(grades).removeGrade("owner");
        order.verify(schedules).removeSchedule("owner");
    }
    @Test void mongoFailureReportsPartialSuccessRatherThanUndoingSql() throws Exception {
        doThrow(new IllegalStateException("synthetic outage")).when(schedules).removeSchedule("owner");
        var failure = assertThrows(CacheClearException.class, () -> service.updateCache(false, "session"));
        assertInstanceOf(IllegalStateException.class, failure.getCause());
        verify(mapper).updateCache(false, "owner"); verify(grades).removeGrade("owner");
        verifyNoMoreInteractions(mapper);
    }
}
