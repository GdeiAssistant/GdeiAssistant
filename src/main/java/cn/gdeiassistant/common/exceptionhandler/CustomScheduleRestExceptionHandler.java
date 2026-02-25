package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.core.schedulequery.controller.ScheduleQueryController;
import cn.gdeiassistant.common.exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.common.exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.common.exception.QueryException.NotAvailableConditionException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ScheduleQueryController.class)
@Order(value = 0)
public class CustomScheduleRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(CustomScheduleRestExceptionHandler.class);

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

    /**
     * 处理该时间段已存在自定义课程的冲突异常
     */
    @ExceptionHandler(NotAvailableConditionException.class)
    public ResponseEntity HandleNotAvailableConditionException(NotAvailableConditionException e) {
        logger.error("CustomScheduleRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }
}
