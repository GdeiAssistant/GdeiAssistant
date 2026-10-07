package cn.gdeiassistant.core.schedulequery.service;

import cn.gdeiassistant.integration.edu.EduSystemClient;
import cn.gdeiassistant.core.userlogin.service.TeacherLoginService;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherScheduleParsingTest {
    private final EduSystemClient client=mock(EduSystemClient.class);
    private final TeacherLoginService login=mock(TeacherLoginService.class);
    private ScheduleService service(){var s=new ScheduleService();ReflectionTestUtils.setField(s,"eduSystemClient",client);ReflectionTestUtils.setField(s,"teacherLoginService",login);return s;}
    private org.jsoup.nodes.Document table(String course,String span){return Jsoup.parse("<table id='Table6'><tr><td>header</td></tr><tr><td>header</td></tr><tr><td>上午</td><td>一</td><td "+span+">"+course+"</td></tr><tr><td>二</td><td>合成课二 必修 9-16周(3节) 教师 地点二 班二</td></tr></table>");}
    @Test void parsesFieldsAndRowspanWithoutDuplicatingCoveredCell() throws Exception {
        when(client.fetchTeacherScheduleDocument("sid","teacher","name","2026","1")).thenReturn(table("合成课 必修 1-8周(1,2节) 教师 地点 班级","rowspan='2'"));
        var courses=service().teacherScheduleQuery("sid","teacher","synthetic","2026","1","name");
        assertEquals(2,courses.size());var first=courses.get(0);
        assertEquals("合成课",first.getScheduleName());assertEquals("必修",first.getScheduleType());assertEquals("1-8周",first.getScheduleWeek());assertEquals("1,2节",first.getScheduleLesson());assertEquals("地点",first.getScheduleLocation());assertEquals("班级",first.getScheduleClass());assertEquals(2,first.getScheduleLength());assertEquals(0,first.getRow());assertEquals(0,first.getColumn());assertEquals(0,first.getPosition());assertNotNull(first.getColorCode());
        assertEquals(8,courses.get(1).getPosition());assertEquals(1,courses.get(1).getColumn());
    }
    @Test void retriesOnlyExpiredSessionAndClassifiesMalformedCourses() throws Exception {
        when(client.fetchTeacherScheduleDocument("sid","teacher","name","2026","1")).thenThrow(new ServerErrorException("需要重新登录")).thenReturn(table("合成课 必修 1-8周(1节) 教师 地点 班级",""));
        assertEquals(2,service().teacherScheduleQuery("sid","teacher","synthetic","2026","1","name").size());verify(login).teacherLogin("sid","teacher","synthetic");
        for(var doc:java.util.List.of(table("缺少信息",""),table("课程 类型 broken 教师 地点 班级",""),table("课程 类型 1-8周(1节) 教师 地点 班级","rowspan='5'"))){
            when(client.fetchTeacherScheduleDocument("sid","teacher","name","2026","1")).thenReturn(doc);
            assertThrows(ServerErrorException.class,()->service().teacherScheduleQuery("sid","teacher","synthetic","2026","1","name"));
        }
        when(client.fetchTeacherScheduleDocument("sid","teacher","name","2026","1")).thenThrow(new IOException("synthetic outage"));
        assertThrows(NetWorkTimeoutException.class,()->service().teacherScheduleQuery("sid","teacher","synthetic","2026","1","name"));
    }
}
