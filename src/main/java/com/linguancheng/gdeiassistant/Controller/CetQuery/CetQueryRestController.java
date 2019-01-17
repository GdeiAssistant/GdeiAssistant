package com.linguancheng.gdeiassistant.Controller.CetQuery;

import com.linguancheng.gdeiassistant.Pojo.CetQuery.CetQuery;
import com.linguancheng.gdeiassistant.Pojo.Entity.Cet;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.CetQuery.CetQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CetQueryRestController {

    @Autowired
    private CetQueryService cetQueryService;

    @RequestMapping(value = "/rest/cetquery", method = RequestMethod.POST)
    public DataJsonResult<Cet> CetQuery(HttpServletRequest request, @Validated CetQuery cetQuery) throws Exception {
        Cet cet = cetQueryService.CetQuery(request.getSession().getId(), cetQuery);
        return new DataJsonResult<>(true, cet);
    }

    @RequestMapping(value = "/api/cet/number", method = RequestMethod.POST)
    public JsonResult SaveCetNumber(HttpServletRequest request, Long number) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        cetQueryService.saveCetNumber(username, number);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/cet/number", method = RequestMethod.GET)
    public DataJsonResult<Long> GetCetNumber(HttpServletRequest request) throws Exception {
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        Long number = cetQueryService.getCetNumber(username);
        return new DataJsonResult<>(true, number);
    }

    @RequestMapping(value = {"/api/cet/checkcode", "/rest/cet/checkcode"}, method = RequestMethod.GET)
    public DataJsonResult<String> GetCheckCodeImage(HttpServletRequest request) throws Exception {
        String checkCode = cetQueryService.CetIndex(request.getSession().getId());
        return new DataJsonResult<>(true, checkCode);
    }
}
