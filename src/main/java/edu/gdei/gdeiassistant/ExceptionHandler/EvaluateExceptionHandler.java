package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ConstantUtils;
import edu.gdei.gdeiassistant.Exception.EvaluateException.NotAvailableTimeException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Integer.MIN_VALUE)
public class EvaluateExceptionHandler {

    @ExceptionHandler(NotAvailableTimeException.class)
    public ResponseEntity HandleNotAvailableTimeException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.EVALUATE_NOT_AVAILABLE
                , false, "现在不是一键评教开放时间段"));
    }
}
