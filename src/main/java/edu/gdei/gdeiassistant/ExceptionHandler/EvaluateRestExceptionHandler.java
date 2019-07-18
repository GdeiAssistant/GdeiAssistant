package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ErrorConstantUtils;
import edu.gdei.gdeiassistant.Controller.Evaluate.RestController.EvaluateRestController;
import edu.gdei.gdeiassistant.Exception.EvaluateException.NotAvailableTimeException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = EvaluateRestController.class)
@Order(value = 0)
public class EvaluateRestExceptionHandler {

    private Log log = LogFactory.getLog(EvaluateRestExceptionHandler.class);

    @ExceptionHandler(NotAvailableTimeException.class)
    public ResponseEntity HandleNotAvailableTimeException(NotAvailableTimeException e) {
        log.error("EvaluateRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.EVALUATE_NOT_AVAILABLE
                , false, "现在不是一键评教开放时间段"));
    }
}
