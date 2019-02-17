package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ConstantUtils;
import edu.gdei.gdeiassistant.Controller.ScheduleQuery.ScheduleQueryRestController;
import edu.gdei.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import edu.gdei.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ScheduleQueryRestController.class)
public class CustomScheduleExceptionHandler {

    /**
     * 处理自定义课程数量超过限制的异常
     *
     * @return
     */
    @ExceptionHandler(CountOverLimitException.class)
    public ResponseEntity HandleCountOverLimitException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.CUSTOM_SCHEDULE_OVER_LIMIT
                , false, "当前位置创建的自定义课程数量超过限制，同一位置只能创建不超过5个自定义课程"));
    }

    /**
     * 处理自定义课程信息校验异常
     *
     * @return
     */
    @ExceptionHandler(GenerateScheduleException.class)
    public ResponseEntity HandleGenerateScheduleException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.INCORRECT_REQUEST_PARAM
                , false, "请求参数不合法"));
    }
}
