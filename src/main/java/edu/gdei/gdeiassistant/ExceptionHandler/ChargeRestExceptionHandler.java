package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ErrorConstantUtils;
import edu.gdei.gdeiassistant.Controller.ChargeRequest.RestController.ChargeRequestRestController;
import edu.gdei.gdeiassistant.Exception.ChargeException.AmountNotAvailableException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ChargeRequestRestController.class)
@Order(value = 1)
public class ChargeRestExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(ChargeRestExceptionHandler.class);

    /**
     * 处理校园卡充值金额不合法异常
     *
     * @return
     */
    @ExceptionHandler(AmountNotAvailableException.class)
    public ResponseEntity HandleAccountNotAvailableException(AmountNotAvailableException e) {
        logger.error("ChargeRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.CHARGE_AMOUNT_NOT_AVAILABLE
                , false, "充值金额不合法"));
    }

}
