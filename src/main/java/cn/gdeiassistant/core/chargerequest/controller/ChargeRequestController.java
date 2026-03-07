package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.annotation.ReplayAttacksProtection;
import cn.gdeiassistant.common.annotation.RequestLogPersistence;
import cn.gdeiassistant.common.annotation.RequireSecurity;
import cn.gdeiassistant.common.annotation.RestAuthentication;
import cn.gdeiassistant.common.pojo.Entity.RequestSecurity;
import cn.gdeiassistant.common.pojo.Entity.RequestValidation;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.charge.pojo.dto.ChargeRequestDTO;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeVO;
import cn.gdeiassistant.core.charge.service.ChargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
public class ChargeRequestController {

    @Autowired
    private ChargeService chargeService;

    @RequestMapping(value = "/api/card/charge", method = RequestMethod.POST)
    @RestAuthentication
    @RequestLogPersistence
    @ReplayAttacksProtection
    @RequireSecurity
    public DataJsonResult<ChargeVO> ChargeRequest(HttpServletRequest request
            , @Validated @NotNull @Min(1) @Max(500) Integer amount
            , @Validated ChargeRequestDTO requestParams
            , @Validated RequestValidation requestValidation
            , @Validated RequestSecurity requestSecurity) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        ChargeVO charge = chargeService.ChargeRequest(sessionId, amount);
        chargeService.SaveChargeLog(sessionId, amount);
        return new DataJsonResult<>(true, charge);
    }
}
