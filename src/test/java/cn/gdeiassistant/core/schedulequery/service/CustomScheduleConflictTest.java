package cn.gdeiassistant.core.schedulequery.service;
import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.common.pojo.document.CustomScheduleDocument;
import cn.gdeiassistant.common.exception.queryexception.NotAvailableConditionException;
import cn.gdeiassistant.common.exception.customscheduleexception.GenerateScheduleException;
import cn.gdeiassistant.common.tools.utils.ScheduleUtils;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CustomScheduleConflictTest {
    private CustomSchedule course(int position,int length,int min,int max){var c=new CustomSchedule();c.setPosition(position);c.setScheduleLength(length);c.setMinScheduleWeek(min);c.setMaxScheduleWeek(max);c.setScheduleName("合成课程");c.setScheduleLocation("合成教室");return c;}
    @Test void overlappingDayLessonsAndWeeksAreRejectedButAdjacentWeeksDaysAndRowsAreAllowed() throws Exception {
        var service=new ScheduleService();var mapper=mock(ScheduleDao.class);var certificate=mock(UserCertificateService.class);ReflectionTestUtils.setField(service,"scheduleDao",mapper);ReflectionTestUtils.setField(service,"userCertificateService",certificate);when(certificate.getUserLoginCertificate("session")).thenReturn(new User("synthetic-owner"));
        var document=new CustomScheduleDocument();var existing=ScheduleUtils.generateCustomSchedule(course(0,2,1,8));document.setScheduleMap(Map.of("existing",existing));when(mapper.queryCustomSchedule("synthetic-owner")).thenReturn(document);
        assertThrows(NotAvailableConditionException.class,()->service.addCustomSchedule("session",course(7,2,8,10)));verify(mapper,never()).addCustomSchedule(anyString(),any());
        service.addCustomSchedule("session",course(7,2,9,10));service.addCustomSchedule("session",course(1,2,1,8));service.addCustomSchedule("session",course(14,1,1,8));verify(mapper,times(3)).addCustomSchedule(eq("synthetic-owner"),any());
        existing.setPosition(null);service.addCustomSchedule("session",course(0,1,1,8));existing.setPosition(0);existing.setRow(null);existing.setScheduleLength(null);existing.setMinScheduleWeek(null);existing.setMaxScheduleWeek(null);
        assertThrows(NotAvailableConditionException.class,()->service.addCustomSchedule("session",course(0,1,1,8)));
    }
    @Test void invalidBoundsNeverGenerateAndValidLastCellHasStableLessonAndColor(){
        for(CustomSchedule invalid:List.of(course(-1,1,1,2),course(70,1,1,2),course(0,0,1,2),course(0,6,1,2),course(63,2,1,2),course(0,1,0,2),course(0,1,1,21),course(0,1,5,4)))assertThrows(GenerateScheduleException.class,()->ScheduleUtils.generateCustomSchedule(invalid));
        assertThrows(GenerateScheduleException.class,()->ScheduleUtils.generateCustomSchedule(null));
        var last=assertDoesNotThrow(()->ScheduleUtils.generateCustomSchedule(course(69,1,1,20)));assertEquals("周日第10节",last.getScheduleLesson());assertEquals(9,last.getRow());assertEquals(6,last.getColumn());assertNotNull(last.getColorCode());assertNotNull(last.getId());
        assertEquals("周一第1,2,3节",assertDoesNotThrow(()->ScheduleUtils.generateCustomSchedule(course(0,3,1,20))).getScheduleLesson());
    }
}
