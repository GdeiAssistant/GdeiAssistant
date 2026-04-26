package cn.gdeiassistant.core.gradequery.service;

import cn.gdeiassistant.common.pojo.Document.GradeDocument;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import cn.gdeiassistant.core.cron.mapper.CronMapper;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeCronServiceTest {

    @Mock
    private UserLoginService userLoginService;

    @Mock
    private GradeService gradeService;

    @Mock
    private CronMapper cronMapper;

    @Mock
    private GradeDao gradeDao;

    @Mock
    private CampusCredentialService campusCredentialService;

    @InjectMocks
    private GradeCronService gradeCronService;

    @Test
    void synchronizeGradeDataOnlyProcessesUsersAllowedByEffectiveQuickAuth() {
        User activeUser = new User("active-user", "saved-credential");
        User revokedUser = new User("revoked-user", "saved-credential");
        User disabledUser = new User("disabled-user", "saved-credential");
        List<User> cacheAllowUsers = List.of(activeUser, revokedUser, disabledUser);

        GradeDocument freshGrade = new GradeDocument();
        freshGrade.setUsername("active-user");
        freshGrade.setUpdateDateTime(new Date());

        when(cronMapper.selectCacheAllowUsers()).thenReturn(cacheAllowUsers);
        when(campusCredentialService.filterUsersWithEffectiveQuickAuth(cacheAllowUsers))
                .thenReturn(List.of(activeUser));
        when(gradeDao.queryGrade("active-user")).thenReturn(freshGrade);

        gradeCronService.synchronizeGradeData();

        verify(cronMapper).selectCacheAllowUsers();
        verify(campusCredentialService).filterUsersWithEffectiveQuickAuth(cacheAllowUsers);
        verify(gradeDao).queryGrade("active-user");
        verifyNoMoreInteractions(gradeDao);
    }
}
