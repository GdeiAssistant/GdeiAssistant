package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.core.evaluate.controller.EvaluateController;
import cn.gdeiassistant.common.exception.EvaluateException.NotAvailableTimeException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = EvaluateController.class)
@Order(value = 0)
public class EvaluateRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(EvaluateRestExceptionHandler.class);

    @ExceptionHandler(NotAvailableTimeException.class)
    public ResponseEntity HandleNotAvailableTimeException(NotAvailableTimeException e) {
        logger.error("EvaluateRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.EVALUATE_NOT_AVAILABLE
                , false, "现在不是一键评教开放时间段"));
    }
}
