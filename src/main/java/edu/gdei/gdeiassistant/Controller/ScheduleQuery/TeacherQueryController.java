package edu.gdei.gdeiassistant.Controller.ScheduleQuery;

import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Pojo.Entity.Teacher;
import edu.gdei.gdeiassistant.Pojo.Entity.TeacherSchedule;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Service.ScheduleQuery.TeacherQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class TeacherQueryController {

    @Autowired
    private TeacherQueryService teacherQueryService;

    /**
     * 教师个人课表查询Restful接口
     *
     * @param request
     * @param teacher
     * @param year
     * @param term
     * @param teacherName
     * @return
     */
    @RequestMapping(value = "/rest/teacher/schedulequery", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<List<TeacherSchedule>> TeacherScheduleQuery(HttpServletRequest request
            , @Validated Teacher teacher, @RequestParam("year") String year, @RequestParam("term") String term
            , @RequestParam(value = "teacherName", required = false) String teacherName) throws NetWorkTimeoutException, ServerErrorException, PasswordIncorrectException {
        List<TeacherSchedule> teacherScheduleList = teacherQueryService.TeacherScheduleQuery(request.getSession().getId(), teacher.getUsername()
                , teacher.getPassword(), year, term, teacherName);
        return new DataJsonResult<>(true, teacherScheduleList);
    }
}
