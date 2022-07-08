package cn.gdeiassistant.Controller.AcademicAffairs.Evaluate.RestController;

import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Annotation.TrialData;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AcademicAffairs.Evaluate.EvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class EvaluateRestController {

    @Autowired
    private EvaluateService evaluateService;

    @RequestMapping(value = "/api/evaluate", method = RequestMethod.POST)
    @TrialData(value = "evaluate", rest = false)
    public JsonResult StartEvaluate(HttpServletRequest request, boolean directlySubmit) throws Exception {
        evaluateService.TeacherEvaluate(request.getSession().getId(), directlySubmit);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/rest/evaluate", method = RequestMethod.POST)
    @RestAuthentication
    @TrialData(value = "evaluate", rest = true)
    public JsonResult StartEvaluate(HttpServletRequest request, @RequestParam("token") String token
            , boolean directlySubmit) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        evaluateService.TeacherEvaluate(sessionId, directlySubmit);
        return new JsonResult(true);
    }
}
