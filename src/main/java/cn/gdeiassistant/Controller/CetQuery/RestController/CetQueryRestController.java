package cn.gdeiassistant.Controller.CetQuery.RestController;

import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Pojo.CetQuery.CetQuery;
import cn.gdeiassistant.Pojo.Entity.Cet;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AcademicAffairs.CetQuery.CetQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CetQueryRestController {

    @Autowired
    private CetQueryService cetQueryService;

    @RequestMapping(value = "/api/cet/number", method = RequestMethod.POST)
    public JsonResult SaveCetNumber(HttpServletRequest request, Long number) throws Exception {
        cetQueryService.saveCetNumber(request.getSession().getId(), number);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/cet/number", method = RequestMethod.GET)
    public DataJsonResult<Long> GetCetNumber(HttpServletRequest request) throws Exception {
        Long number = cetQueryService.getCetNumber(request.getSession().getId());
        return new DataJsonResult<>(true, number);
    }

    @RequestMapping(value = "/api/cet/checkcode", method = RequestMethod.GET)
    public DataJsonResult<String> GetCheckCodeImage(HttpServletRequest request) throws Exception {
        String checkCode = cetQueryService.CetIndex(request.getSession().getId());
        return new DataJsonResult<>(true, checkCode);
    }

    @RequestMapping(value = "/rest/cet/checkcode", method = RequestMethod.GET)
    @RestAuthentication
    public DataJsonResult<String> GetCheckCodeImage(HttpServletRequest request
            , @RequestParam("token") String token) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        String checkCode = cetQueryService.CetIndex(sessionId);
        return new DataJsonResult<>(true, checkCode);
    }

    @RequestMapping(value = "/rest/cetquery", method = RequestMethod.POST)
    public DataJsonResult<Cet> CetQuery(HttpServletRequest request
            , @RequestParam("token") String token
            , @Validated CetQuery cetQuery) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        Cet cet = cetQueryService.CetQuery(sessionId, cetQuery);
        return new DataJsonResult<>(true, cet);
    }
}
