package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.CommonException.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.TestAccountException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.common.exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.apache.http.MethodNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(annotations = RestController.class)
@Order(value = 2)
public class GlobalRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    /**
     * 处理HTTP请求400错误
     *
     * @return
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, TypeMismatchException.class
            , HttpMessageNotReadableException.class})
    public ResponseEntity HandleBadRequestException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false
                , "请求参数不合法"));
    }

    /**
     * 处理HTTP请求405错误
     *
     * @return
     */
    @ExceptionHandler(MethodNotSupportedException.class)
    public ResponseEntity HandleMethodNotSupportedException() {
        return ResponseEntity.ok(new JsonResult(false, "请求方法不支持"));
    }

    /**
     * 处理数据校验异常
     *
     * @return
     */
    @ExceptionHandler({ConstraintViolationException.class, BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity HandleConstraintViolationException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false
                , "请求参数不合法"));
    }

    /**
     * 处理查询数据不存在的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity HandleDataNotExistException(DataNotExistException e) {
        logger.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.DATA_NOT_EXIST, false, e.getMessage()));
    }

    /**
     * 处理网络连接超时异常
     *
     * @return
     */
    @ExceptionHandler(NetWorkTimeoutException.class)
    public ResponseEntity HandleNetWorkTimeoutException(NetWorkTimeoutException e) {
        logger.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.NETWORK_TIMEOUT, false, "网络连接超时，请重试"));
    }

    /**
     * 处理查询条件不合法的异常
     *
     * @return
     */
    @ExceptionHandler(ErrorQueryConditionException.class)
    public ResponseEntity HandleErrorQueryConditionException(ErrorQueryConditionException e) {
        logger.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ERROR_QUERY_CONDITION, false, "查询条件不合法，请重新填写"));
    }

    /**
     * 处理时间戳校验失败的异常
     *
     * @return
     */
    @ExceptionHandler(TimeStampIncorrectException.class)
    public ResponseEntity HandleTimeStampIncorrectException(TimeStampIncorrectException e) {
        logger.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.TIMESTAMP_INVALIDATED, false, "时间戳校验失败，请尝试重新登录"));
    }

    /**
     * 处理用户账号密码错误的异常
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException(PasswordIncorrectException e) {
        logger.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.PASSWORD_INCORRECT
                , false, "用户账号密码错误，请检查重试或重新登录"));
    }

    /**
     * 处理用户不存在的异常
     *
     * @return
     */
    @ExceptionHandler(UserNotExistException.class)
    public ResponseEntity HandleUserNotExistException(UserNotExistException e) {
        logger.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.USER_NOT_EXIST
                , false, "当前用户不存在，请尝试重新登录"));
    }

    /**
     * 处理测试账号受限操作的异常
     *
     * @return
     */
    @ExceptionHandler(TestAccountException.class)
    public ResponseEntity HandleTestAccountException(TestAccountException e) {
        logger.error("GlobalRestExceptionHandler：", e);
        String message = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : "测试用户不支持该功能";
        return ResponseEntity.ok(new JsonResult(false, message));
    }

    /**
     * 功能未启用（特性开关 / 降级）
     */
    @ExceptionHandler(FeatureNotEnabledException.class)
    public ResponseEntity HandleFeatureNotEnabledException(FeatureNotEnabledException e) {
        logger.warn("FeatureNotEnabledException: {}", e.getMessage());
        String message = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : "该功能未启用";
        return ResponseEntity.ok(new JsonResult(false, message));
    }

    /**
     * 处理系统异常（数据库、Redis、空指针等底层技术异常不暴露给前端，仅打日志）
     * 返回 HTTP 500，符合 RESTful 规范，便于网络监控识别服务端错误。
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResult> HandleException(Exception e) {
        logger.error("系统内部异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonResult(ErrorConstantUtils.INTERNAL_SERVER_ERROR, false, "系统繁忙，请稍后再试"));
    }
}
