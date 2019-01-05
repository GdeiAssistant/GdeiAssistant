package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Constant.ConstantUtils;
import com.linguancheng.gdeiassistant.Controller.ChargeRequest.ChargeRequestController;
import com.linguancheng.gdeiassistant.Exception.ChargeException.AmountNotAvailableException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ChargeRequestController.class)
@Order(Integer.MIN_VALUE)
public class ChargeExceptionHandler {

    /**
     * 处理校园卡充值金额不合法异常
     *
     * @return
     */
    @ExceptionHandler(AmountNotAvailableException.class)
    public ResponseEntity HandleAccountNotAvailableException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.CHARGE_AMOUNT_NOT_AVAILABLE
                , false, "充值金额不合法"));
    }

}
