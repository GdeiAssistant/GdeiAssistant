package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.authenticationexception.AuthenticationRecordExistException;
import cn.gdeiassistant.common.exception.authenticationexception.InconsistentAuthenticationException;
import cn.gdeiassistant.common.exception.authenticationexception.NullIDPhotoException;
import cn.gdeiassistant.common.exception.chargeexception.AmountNotAvailableException;
import cn.gdeiassistant.common.exception.chargeexception.ChargeIdempotencyException;
import cn.gdeiassistant.common.exception.closeaccountexception.ItemAvailableException;
import cn.gdeiassistant.common.exception.closeaccountexception.UserStateErrorException;
import cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.customscheduleexception.CountOverLimitException;
import cn.gdeiassistant.common.exception.customscheduleexception.GenerateScheduleException;
import cn.gdeiassistant.common.exception.databaseexception.ConfirmedStateException;
import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.exception.databaseexception.NoAccessException;
import cn.gdeiassistant.common.exception.databaseexception.NotAvailableStateException;
import cn.gdeiassistant.common.exception.databaseexception.UserNotExistException;
import cn.gdeiassistant.common.exception.datingexception.RepeatPickException;
import cn.gdeiassistant.common.exception.datingexception.SelfPickException;
import cn.gdeiassistant.common.exception.deliveryexception.DeliveryOrderStateUpdatedException;
import cn.gdeiassistant.common.exception.deliveryexception.DeliveryOrderTakenException;
import cn.gdeiassistant.common.exception.deliveryexception.NoAccessUpdatingException;
import cn.gdeiassistant.common.exception.deliveryexception.SelfTradingOrderException;
import cn.gdeiassistant.common.exception.evaluateexception.NotAvailableTimeException;
import cn.gdeiassistant.common.exception.expressexception.CorrectRecordException;
import cn.gdeiassistant.common.exception.expressexception.NoRealNameException;
import cn.gdeiassistant.common.exception.bookrenewexception.BookRenewOvertimeException;
import cn.gdeiassistant.common.exception.queryexception.ErrorQueryConditionException;
import cn.gdeiassistant.common.exception.queryexception.NotAvailableConditionException;
import cn.gdeiassistant.common.exception.queryexception.TimeStampIncorrectException;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.common.exception.verificationexception.DayFrequencyLimitException;
import cn.gdeiassistant.common.exception.verificationexception.HourFrequencyLimitException;
import cn.gdeiassistant.common.exception.verificationexception.IllegalPhoneNumberException;
import cn.gdeiassistant.common.exception.verificationexception.MinuteFrequencyLimitException;
import cn.gdeiassistant.common.exception.verificationexception.SendEmailException;
import cn.gdeiassistant.common.exception.verificationexception.SendSMSException;
import cn.gdeiassistant.common.exception.verificationexception.VerificationCodeInvalidException;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<JsonResult> handleBadRequestException(HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false,
                BackendTextLocalizer.localizeMessage("请求参数不合法", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(MethodNotSupportedException.class)
    public ResponseEntity<JsonResult> handleMethodNotSupportedException(HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage("请求方法不支持", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({ConstraintViolationException.class, BindException.class, MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class})
    public ResponseEntity<JsonResult> handleConstraintViolationException(HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false,
                BackendTextLocalizer.localizeMessage("请求参数不合法", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonResult> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false,
                BackendTextLocalizer.localizeMessage(
                        e.getMessage() != null ? e.getMessage() : "请求参数不合法",
                        request.getHeader("Accept-Language"))));
    }

    // ========== 通用业务异常（使用异常自身 message） ==========

    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity<JsonResult> handleDataNotExistException(DataNotExistException e, HttpServletRequest request) {
        logException("数据不存在：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.DATA_NOT_EXIST, false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(NetWorkTimeoutException.class)
    public ResponseEntity<JsonResult> handleNetWorkTimeoutException(NetWorkTimeoutException e, HttpServletRequest request) {
        logException("网络超时：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.NETWORK_TIMEOUT, false,
                BackendTextLocalizer.localizeMessage("网络连接超时，请重试", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(ErrorQueryConditionException.class)
    public ResponseEntity<JsonResult> handleErrorQueryConditionException(ErrorQueryConditionException e, HttpServletRequest request) {
        logException("查询条件错误：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ERROR_QUERY_CONDITION, false,
                BackendTextLocalizer.localizeMessage("查询条件不合法，请重新填写", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(TimeStampIncorrectException.class)
    public ResponseEntity<JsonResult> handleTimeStampIncorrectException(TimeStampIncorrectException e, HttpServletRequest request) {
        logException("时间戳校验失败：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.TIMESTAMP_INVALIDATED, false,
                BackendTextLocalizer.localizeMessage("时间戳校验失败，请尝试重新登录", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity<JsonResult> handlePasswordIncorrectException(PasswordIncorrectException e, HttpServletRequest request) {
        logException("密码错误：", e);
        String message = e.getMessage() != null ? e.getMessage() : "用户账号密码错误，请检查重试或重新登录";
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.PASSWORD_INCORRECT, false,
                BackendTextLocalizer.localizeMessage(message, request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(UserNotExistException.class)
    public ResponseEntity<JsonResult> handleUserNotExistException(UserNotExistException e, HttpServletRequest request) {
        logException("用户不存在：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.USER_NOT_EXIST, false,
                BackendTextLocalizer.localizeMessage("当前用户不存在，请尝试重新登录", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(FeatureNotEnabledException.class)
    public ResponseEntity<JsonResult> handleFeatureNotEnabledException(FeatureNotEnabledException e, HttpServletRequest request) {
        logger.warn("功能未启用: exceptionType={}", e.getClass().getSimpleName());
        String message = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : "该功能未启用";
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(message, request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(RecognitionException.class)
    public ResponseEntity<JsonResult> handleRecognitionException(RecognitionException e, HttpServletRequest request) {
        logger.warn("图像识别异常: exceptionType={}", e.getClass().getSimpleName());
        String message = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : "图像识别服务异常，请稍后重试";
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(message, request.getHeader("Accept-Language"))));
    }

    // ========== 原 module-specific handlers 合并 ==========

    @ExceptionHandler({NoAccessException.class, ConfirmedStateException.class, NotAvailableStateException.class,
            NoAccessUpdatingException.class})
    public ResponseEntity<JsonResult> handleAccessException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({NullIDPhotoException.class, InconsistentAuthenticationException.class,
            AuthenticationRecordExistException.class})
    public ResponseEntity<JsonResult> handleAuthenticationException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({BookRenewOvertimeException.class})
    public ResponseEntity<JsonResult> handleBookRenewException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(AmountNotAvailableException.class)
    public ResponseEntity<JsonResult> handleAmountNotAvailableException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(ChargeIdempotencyException.class)
    public ResponseEntity<JsonResult> handleChargeIdempotencyException(ChargeIdempotencyException e,
                                                                       HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(e.getCode(), false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({UserStateErrorException.class, ItemAvailableException.class})
    public ResponseEntity<JsonResult> handleCloseAccountException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({CountOverLimitException.class, GenerateScheduleException.class,
            NotAvailableConditionException.class})
    public ResponseEntity<JsonResult> handleScheduleException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({RepeatPickException.class, SelfPickException.class})
    public ResponseEntity<JsonResult> handleDatingException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({DeliveryOrderTakenException.class, SelfTradingOrderException.class,
            DeliveryOrderStateUpdatedException.class})
    public ResponseEntity<JsonResult> handleDeliveryException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(NotAvailableTimeException.class)
    public ResponseEntity<JsonResult> handleNotAvailableTimeException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({CorrectRecordException.class, NoRealNameException.class})
    public ResponseEntity<JsonResult> handleExpressException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({HourFrequencyLimitException.class, MinuteFrequencyLimitException.class,
            DayFrequencyLimitException.class, IllegalPhoneNumberException.class,
            VerificationCodeInvalidException.class, SendSMSException.class, SendEmailException.class})
    public ResponseEntity<JsonResult> handleVerificationException(Exception e, HttpServletRequest request) {
        logger.warn("VerificationFailure - exceptionType={}", e.getClass().getSimpleName());
        return ResponseEntity.ok(new JsonResult(false,
                BackendTextLocalizer.localizeMessage(e.getMessage(), request.getHeader("Accept-Language"))));
    }

    // Preserve diagnostic locations, but never exception messages or provider response bodies.
    private void logException(String event, Exception exception) {
        logger.error("{} exceptionType={} stack={}", event, exception.getClass().getSimpleName(),
                java.util.Arrays.toString(exception.getStackTrace()));
    }

    // ========== 兜底 ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResult> handleException(Exception e, HttpServletRequest request) {
        logException("系统内部异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonResult(ErrorConstantUtils.INTERNAL_SERVER_ERROR, false,
                        BackendTextLocalizer.localizeMessage("系统繁忙，请稍后再试", request.getHeader("Accept-Language"))));
    }
}
