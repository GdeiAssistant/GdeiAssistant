package com.linguancheng.gdeiassistant.Controller.ChargeRequest;

import com.linguancheng.gdeiassistant.Annotation.ReplayAttacksProtection;
import com.linguancheng.gdeiassistant.Annotation.RequestLogPersistence;
import com.linguancheng.gdeiassistant.Annotation.RequireSecurity;
import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Pojo.Charge.ChargeRequest;
import com.linguancheng.gdeiassistant.Pojo.Entity.Charge;
import com.linguancheng.gdeiassistant.Pojo.Entity.RequestSecurity;
import com.linguancheng.gdeiassistant.Pojo.Entity.RequestValidation;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.Charge.ChargeService;
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
public class ChargeRequestController {

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