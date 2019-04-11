package edu.gdei.gdeiassistant.Controller.ChargeRequest;

import edu.gdei.gdeiassistant.Annotation.ReplayAttacksProtection;
import edu.gdei.gdeiassistant.Annotation.RequestLogPersistence;
import edu.gdei.gdeiassistant.Annotation.RequireSecurity;
import edu.gdei.gdeiassistant.Annotation.RestAuthentication;
import edu.gdei.gdeiassistant.Pojo.Charge.ChargeRequest;
import edu.gdei.gdeiassistant.Pojo.Entity.Charge;
import edu.gdei.gdeiassistant.Pojo.Entity.RequestSecurity;
import edu.gdei.gdeiassistant.Pojo.Entity.RequestValidation;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Service.Charge.ChargeService;
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
        User user = (User) request.getAttribute("user");
        Charge charge = chargeService.ChargeRequest(request.getSession().getId()
                , user.getUsername(), user.getPassword(), amount);
        //保存充值日志记录
        chargeService.SaveChargeLog(user.getUsername(), amount);
        //返回充值结果
        return new DataJsonResult<>(true, charge);
    }
}