package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.AuthenticationException.AuthenticationRecordExistException;
import cn.gdeiassistant.common.exception.AuthenticationException.InconsistentAuthenticationException;
import cn.gdeiassistant.common.exception.AuthenticationException.NullIDPhotoException;
import cn.gdeiassistant.common.exception.ChargeException.AmountNotAvailableException;
import cn.gdeiassistant.common.exception.CloseAccountException.ItemAvailableException;
import cn.gdeiassistant.common.exception.CloseAccountException.UserStateErrorException;
import cn.gdeiassistant.common.exception.CommonException.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.TestAccountException;
import cn.gdeiassistant.common.exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.common.exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.common.exception.DatabaseException.ConfirmedStateException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.exception.DatabaseException.NotAvailableStateException;
import cn.gdeiassistant.common.exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.common.exception.DatingException.RepeatPickException;
import cn.gdeiassistant.common.exception.DatingException.SelfPickException;
import cn.gdeiassistant.common.exception.DeliveryException.DeliveryOrderStateUpdatedException;
import cn.gdeiassistant.common.exception.DeliveryException.DeliveryOrderTakenException;
import cn.gdeiassistant.common.exception.DeliveryException.NoAccessUpdatingException;
import cn.gdeiassistant.common.exception.DeliveryException.SelfTradingOrderException;
import cn.gdeiassistant.common.exception.EvaluateException.NotAvailableTimeException;
import cn.gdeiassistant.common.exception.ExpressException.CorrectRecordException;
import cn.gdeiassistant.common.exception.ExpressException.NoRealNameException;
import cn.gdeiassistant.common.exception.BookRenewException.BookRenewOvertimeException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.common.exception.QueryException.NotAvailableConditionException;
import cn.gdeiassistant.common.exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.common.exception.VerificationException.DayFrequencyLimitException;
import cn.gdeiassistant.common.exception.VerificationException.HourFrequencyLimitException;
import cn.gdeiassistant.common.exception.VerificationException.IllegalPhoneNumberException;
import cn.gdeiassistant.common.exception.VerificationException.MinuteFrequencyLimitException;
import cn.gdeiassistant.common.exception.VerificationException.SendEmailException;
import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;
import cn.gdeiassistant.common.exception.VerificationException.VerificationCodeInvalidException;
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

/**
 * 统一 REST API 异常处理器。
 * 原先分散在 17 个 module-specific handler 中的逻辑统一收归此处。
 * 所有业务异常返回 HTTP 200 + JsonResult(false, message)，
 * 由异常自身携带的 message 提供上下文信息。
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(value = 2)
public class GlobalRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    // ========== HTTP 请求级别错误 ==========

    @ExceptionHandler({MissingServletRequestParameterException.class, TypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<JsonResult> handleBadRequestException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false,
                "请求参数不合法"));
    }

    @ExceptionHandler(MethodNotSupportedException.class)
    public ResponseEntity<JsonResult> handleMethodNotSupportedException() {
        return ResponseEntity.ok(new JsonResult(false, "请求方法不支持"));
    }

    @ExceptionHandler({ConstraintViolationException.class, BindException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<JsonResult> handleConstraintViolationException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false,
                "请求参数不合法"));
    }

    // ========== 通用业务异常（使用异常自身 message） ==========

    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity<JsonResult> handleDataNotExistException(DataNotExistException e) {
        logger.error("数据不存在：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.DATA_NOT_EXIST, false, e.getMessage()));
    }

    @ExceptionHandler(NetWorkTimeoutException.class)
    public ResponseEntity<JsonResult> handleNetWorkTimeoutException(NetWorkTimeoutException e) {
        logger.error("网络超时：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.NETWORK_TIMEOUT, false, "网络连接超时，请重试"));
    }

    @ExceptionHandler(ErrorQueryConditionException.class)
    public ResponseEntity<JsonResult> handleErrorQueryConditionException(ErrorQueryConditionException e) {
        logger.error("查询条件错误：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ERROR_QUERY_CONDITION, false, "查询条件不合法，请重新填写"));
    }

    @ExceptionHandler(TimeStampIncorrectException.class)
    public ResponseEntity<JsonResult> handleTimeStampIncorrectException(TimeStampIncorrectException e) {
        logger.error("时间戳校验失败：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.TIMESTAMP_INVALIDATED, false, "时间戳校验失败，请尝试重新登录"));
    }

    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity<JsonResult> handlePasswordIncorrectException(PasswordIncorrectException e) {
        logger.error("密码错误：", e);
        String message = e.getMessage() != null ? e.getMessage() : "用户账号密码错误，请检查重试或重新登录";
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.PASSWORD_INCORRECT, false, message));
    }

    @ExceptionHandler(UserNotExistException.class)
    public ResponseEntity<JsonResult> handleUserNotExistException(UserNotExistException e) {
        logger.error("用户不存在：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.USER_NOT_EXIST, false, "当前用户不存在，请尝试重新登录"));
    }

    @ExceptionHandler(TestAccountException.class)
    public ResponseEntity<JsonResult> handleTestAccountException(TestAccountException e) {
        String message = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : "测试用户不支持该功能";
        return ResponseEntity.ok(new JsonResult(false, message));
    }

    @ExceptionHandler(FeatureNotEnabledException.class)
    public ResponseEntity<JsonResult> handleFeatureNotEnabledException(FeatureNotEnabledException e) {
        logger.warn("功能未启用: {}", e.getMessage());
        String message = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : "该功能未启用";
        return ResponseEntity.ok(new JsonResult(false, message));
    }

    // ========== 原 module-specific handlers 合并 ==========

    @ExceptionHandler({NoAccessException.class, ConfirmedStateException.class, NotAvailableStateException.class,
            NoAccessUpdatingException.class})
    public ResponseEntity<JsonResult> handleAccessException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler({NullIDPhotoException.class, InconsistentAuthenticationException.class,
            AuthenticationRecordExistException.class})
    public ResponseEntity<JsonResult> handleAuthenticationException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler({BookRenewOvertimeException.class})
    public ResponseEntity<JsonResult> handleBookRenewException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler(AmountNotAvailableException.class)
    public ResponseEntity<JsonResult> handleAmountNotAvailableException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler({UserStateErrorException.class, ItemAvailableException.class})
    public ResponseEntity<JsonResult> handleCloseAccountException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler({CountOverLimitException.class, GenerateScheduleException.class,
            NotAvailableConditionException.class})
    public ResponseEntity<JsonResult> handleScheduleException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler({RepeatPickException.class, SelfPickException.class})
    public ResponseEntity<JsonResult> handleDatingException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler({DeliveryOrderTakenException.class, SelfTradingOrderException.class,
            DeliveryOrderStateUpdatedException.class})
    public ResponseEntity<JsonResult> handleDeliveryException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler(NotAvailableTimeException.class)
    public ResponseEntity<JsonResult> handleNotAvailableTimeException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler({CorrectRecordException.class, NoRealNameException.class})
    public ResponseEntity<JsonResult> handleExpressException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    @ExceptionHandler({HourFrequencyLimitException.class, MinuteFrequencyLimitException.class,
            DayFrequencyLimitException.class, IllegalPhoneNumberException.class,
            VerificationCodeInvalidException.class, SendSMSException.class, SendEmailException.class})
    public ResponseEntity<JsonResult> handleVerificationException(Exception e) {
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

    // ========== 兜底 ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResult> handleException(Exception e) {
        logger.error("系统内部异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonResult(ErrorConstantUtils.INTERNAL_SERVER_ERROR, false, "系统繁忙，请稍后再试"));
    }
}
