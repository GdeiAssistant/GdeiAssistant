package edu.gdei.gdeiassistant.Controller.Account;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Enum.Graduation.GraduationProgramTypeEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.Graduation;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Account.GraduatedAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
public class GraduatedAccountRestController {

    @Autowired
    private GraduatedAccountService graduatedAccountService;

    /**
     * 查询用户选择的毕业用户账号处理方案
     *
     * @param request
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/graduation", method = RequestMethod.GET)
    public DataJsonResult<Graduation> QueryGraduation(HttpServletRequest request) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        Graduation graduation = graduatedAccountService.QueryUserGraduationProgram(username);
        return new DataJsonResult<>(true, graduation);
    }

    /**
     * 保存用户选择的毕业用户账号处理方案
     *
     * @param request
     * @param program
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/graduation", method = RequestMethod.POST)
    public JsonResult SaveGraduation(HttpServletRequest request, @Validated @NotNull Integer program) throws WsgException {
        if (program < 0 || program >= GraduationProgramTypeEnum.values().length) {
            return new JsonResult(false, "请求参数不合法");
        }
        String username = (String) request.getSession().getAttribute("username");
        graduatedAccountService.SaveUserGraduationProgram(username, program);
        return new JsonResult(true);
    }

}
