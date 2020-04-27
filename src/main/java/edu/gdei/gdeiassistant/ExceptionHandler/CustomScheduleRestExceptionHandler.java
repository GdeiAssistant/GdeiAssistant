package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ErrorConstantUtils;
import edu.gdei.gdeiassistant.Controller.ScheduleQuery.RestController.ScheduleQueryRestController;
import edu.gdei.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import edu.gdei.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ScheduleQueryRestController.class)
@Order(value = 0)
public class CustomScheduleRestExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(CustomScheduleRestExceptionHandler.class);

    /**
     * 处理自定义课程数量超过限制的异常
     *
     * @return
     */
    @ExceptionHandler(CountOverLimitException.class)
    public ResponseEntity HandleCountOverLimitException(CountOverLimitException e) {
        logger.error("CustomScheduleRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.CUSTOM_SCHEDULE_OVER_LIMIT
                , false, "当前位置创建的自定义课程数量超过限制，同一位置只能创建不超过5个自定义课程"));
    }

    /**
     * 处理自定义课程信息校验异常
     *
     * @return
     */
    @ExceptionHandler(GenerateScheduleException.class)
    public ResponseEntity HandleGenerateScheduleException(GenerateScheduleException e) {
        logger.error("CustomScheduleRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM
                , false, "请求参数不合法"));
    }
}
