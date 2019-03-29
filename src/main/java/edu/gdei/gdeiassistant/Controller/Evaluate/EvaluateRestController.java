package edu.gdei.gdeiassistant.Controller.Evaluate;

import edu.gdei.gdeiassistant.Annotation.RestAuthentication;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Evaluate.EvaluateService;
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
    public JsonResult StartEvaluate(HttpServletRequest request, boolean directlySubmit) throws Exception {
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        String password = (String) WebUtils.getSessionAttribute(request, "password");
        evaluateService.SyncSessionAndEvaluate(request.getSession().getId()
                , new User(username, password), directlySubmit);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/rest/evaluate", method = RequestMethod.POST)
    @RestAuthentication
    public JsonResult StartEvaluate(HttpServletRequest request, @RequestParam("token") String token
            , boolean directlySubmit) throws Exception {
        User user = (User) request.getAttribute("user");
        evaluateService.SyncSessionAndEvaluate(request.getSession().getId(), user, directlySubmit);
        return new JsonResult(true);
    }
}
