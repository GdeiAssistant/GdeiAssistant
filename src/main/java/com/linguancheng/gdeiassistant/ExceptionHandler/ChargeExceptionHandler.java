package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Constant.ConstantUtils;
import com.linguancheng.gdeiassistant.Controller.ChargeRequest.ChargeRequestController;
import com.linguancheng.gdeiassistant.Exception.ChargeException.AmountNotAvailableException;
import com.linguancheng.gdeiassistant.Exception.ChargeException.SecurityInvalidException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ChargeRequestController.class)
public class ChargeExceptionHandler {

    @ExceptionHandler(AmountNotAvailableException.class)
    public ResponseEntity HandleAccountNotAvailableException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.CHARGE_AMOUNT_NOT_AVAILABLE
                , false, "充值金额不合法"));
    }

    @ExceptionHandler(SecurityInvalidException.class)
    public ResponseEntity HandleSecurityInvalidException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.CHARGE_SECURITY_INVALID
                , false, "充值安全校验不通过"));
    }

}
