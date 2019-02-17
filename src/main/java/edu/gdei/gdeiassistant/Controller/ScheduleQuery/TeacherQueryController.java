package edu.gdei.gdeiassistant.Controller.ScheduleQuery;

import edu.gdei.gdeiassistant.Enum.Base.ServiceResultEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.Teacher;
import edu.gdei.gdeiassistant.Pojo.Entity.TeacherSchedule;
import edu.gdei.gdeiassistant.Pojo.Result.BaseResult;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Service.ScheduleQuery.TeacherQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
     * 教师个人课表查询Rest接口
     *
     * @param request
     * @param teacher
     * @param year
     * @param term
     * @param teacherName
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/teacher/schedulequery", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<List<TeacherSchedule>> TeacherScheduleQuery(HttpServletRequest request
            , @Validated Teacher teacher, @RequestParam("year") String year, @RequestParam("term") String term
            , @RequestParam(value = "teacherName", required = false) String teacherName
            , BindingResult bindingResult) {
        DataJsonResult<List<TeacherSchedule>> result = new DataJsonResult<>();
        if (bindingResult.hasErrors()) {
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
        } else {
            BaseResult<List<TeacherSchedule>, ServiceResultEnum> queryResult = teacherQueryService
                    .TeacherScheduleQuery(request.getSession().getId(), teacher.getUsername()
                            , teacher.getPassword(), year, term, teacherName);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    result.setSuccess(true);
                    result.setData(queryResult.getResultData());
                    break;

                case SERVER_ERROR:
                    result.setSuccess(false);
                    result.setMessage("教务系统异常，请稍后再试");
                    break;

                case TIME_OUT:
                    result.setSuccess(false);
                    result.setMessage("网络连接超时，请重试");
                    break;

                case PASSWORD_INCORRECT:
                    result.setSuccess(false);
                    result.setMessage("账号密码错误，请检查并重试");
                    break;
            }
        }
        return result;
    }
}
