package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ConstantUtils;
import edu.gdei.gdeiassistant.Controller.ChargeRequest.ChargeRequestRestController;
import edu.gdei.gdeiassistant.Exception.ChargeException.AmountNotAvailableException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ChargeRequestRestController.class)
@Order(value = 1)
public class ChargeRestExceptionHandler {

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
