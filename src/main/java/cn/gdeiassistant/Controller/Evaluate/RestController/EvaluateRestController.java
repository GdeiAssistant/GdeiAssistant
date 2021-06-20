package cn.gdeiassistant.Controller.Evaluate.RestController;

import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Annotation.TrialData;
import cn.gdeiassistant.Annotation.UserGroupAccess;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Evaluate.EvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;

@RestController
public class EvaluateRestController {

    @Autowired
    private EvaluateService evaluateService;

    @RequestMapping(value = "/api/evaluate", method = RequestMethod.POST)
    @TrialData(value = "evaluate")
    @UserGroupAccess(group = {2, 3, 7}, rest = true)
    public JsonResult StartEvaluate(HttpServletRequest request, boolean directlySubmit) throws Exception {
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        String password = (String) WebUtils.getSessionAttribute(request, "password");
        evaluateService.SyncSessionAndEvaluate(request.getSession().getId()
                , new User(username, password), directlySubmit);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/rest/evaluate", method = RequestMethod.POST)
    @RestAuthentication
    @TrialData(value = "evaluate")
    @UserGroupAccess(group = {2, 3, 7}, rest = true)
    public JsonResult StartEvaluate(HttpServletRequest request, @RequestParam("token") String token
            , boolean directlySubmit) throws Exception {
        User user = (User) request.getAttribute("user");
        evaluateService.SyncSessionAndEvaluate(request.getSession().getId(), user, directlySubmit);
        return new JsonResult(true);
    }
}
