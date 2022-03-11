package cn.gdeiassistant.Controller.ChargeRequest.RestController;

import cn.gdeiassistant.Annotation.ReplayAttacksProtection;
import cn.gdeiassistant.Annotation.RequestLogPersistence;
import cn.gdeiassistant.Annotation.RequireSecurity;
import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Pojo.Charge.ChargeRequest;
import cn.gdeiassistant.Pojo.Entity.Charge;
import cn.gdeiassistant.Pojo.Entity.RequestSecurity;
import cn.gdeiassistant.Pojo.Entity.RequestValidation;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.Charge.ChargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
public class ChargeRequestRestController {

    @Autowired
    private ChargeService chargeService;

    @RequestMapping(value = "/rest/charge", method = RequestMethod.POST)
    @RestAuthentication
    @RequestLogPersistence
    @ReplayAttacksProtection
    @RequireSecurity
    public DataJsonResult ChargeRequest(HttpServletRequest request
            , @RequestParam("token") String token
            , @Validated @NotNull @Min(1) @Max(500) Integer amount
            , @Validated ChargeRequest requestParams
            , @Validated RequestValidation requestValidation
            , @Validated RequestSecurity requestSecurity) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        Charge charge = chargeService.ChargeRequest(sessionId, amount);
        //保存充值日志记录
        chargeService.SaveChargeLog(sessionId, amount);
        //返回充值结果
        return new DataJsonResult<>(true, charge);
    }
}