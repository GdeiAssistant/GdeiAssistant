package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.authenticationexception.AuthenticationRecordExistException;
import cn.gdeiassistant.common.exception.authenticationexception.IDPhotoCountLimitationException;
import cn.gdeiassistant.common.exception.authenticationexception.IDPhotoSizeLimitationException;
import cn.gdeiassistant.common.exception.authenticationexception.InconsistentAuthenticationException;
import cn.gdeiassistant.common.exception.authenticationexception.NullIDPhotoException;
import cn.gdeiassistant.common.exception.chargeexception.AmountNotAvailableException;
import cn.gdeiassistant.common.exception.chargeexception.ChargeIdempotencyException;
import cn.gdeiassistant.common.exception.closeaccountexception.ItemAvailableException;
import cn.gdeiassistant.common.exception.closeaccountexception.UserStateErrorException;
import cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
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
import cn.gdeiassistant.common.i18n.KeyedMessage;
import cn.gdeiassistant.common.i18n.MessageCatalog;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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
import cn.gdeiassistant.common.i18n.Messages;

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
                Messages.forLanguage("request.invalid", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<JsonResult> handleMethodNotSupportedException(HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                Messages.forLanguage("request.methodUnsupported", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler({ConstraintViolationException.class, BindException.class, MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class})
    public ResponseEntity<JsonResult> handleConstraintViolationException(HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false,
                Messages.forLanguage("request.invalid", request.getHeader("Accept-Language"))));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonResult> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, false,
                resolveExceptionMessage(e, request, "请求参数不合法")));
    }

    // ========== 通用业务异常（优先使用 KeyedMessage，未迁移异常保留文案 fallback） ==========

    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity<JsonResult> handleDataNotExistException(DataNotExistException e, HttpServletRequest request) {
        logException("数据不存在：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.DATA_NOT_EXIST, false,
                resolveExceptionMessage(e, request, null)));
    }

    @ExceptionHandler(NetWorkTimeoutException.class)
    public ResponseEntity<JsonResult> handleNetWorkTimeoutException(NetWorkTimeoutException e, HttpServletRequest request) {
        logException("网络超时：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.NETWORK_TIMEOUT, false,
                resolveKeyOrDefault(e, request, "request.networkTimeoutRetry")));
    }

    @ExceptionHandler(ErrorQueryConditionException.class)
    public ResponseEntity<JsonResult> handleErrorQueryConditionException(ErrorQueryConditionException e, HttpServletRequest request) {
        logException("查询条件错误：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ERROR_QUERY_CONDITION, false,
                resolveKeyOrDefault(e, request, "query.invalidConditionFilled")));
    }

    @ExceptionHandler(TimeStampIncorrectException.class)
    public ResponseEntity<JsonResult> handleTimeStampIncorrectException(TimeStampIncorrectException e, HttpServletRequest request) {
        logException("时间戳校验失败：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.TIMESTAMP_INVALIDATED, false,
                resolveKeyOrDefault(e, request, "request.timestampInvalid")));
    }

    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity<JsonResult> handlePasswordIncorrectException(PasswordIncorrectException e, HttpServletRequest request) {
        logException("密码错误：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.PASSWORD_INCORRECT, false,
                resolveExceptionMessage(e, request, "用户账号密码错误，请检查重试或重新登录")));
    }

    @ExceptionHandler(UserNotExistException.class)
    public ResponseEntity<JsonResult> handleUserNotExistException(UserNotExistException e, HttpServletRequest request) {
        logException("用户不存在：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.USER_NOT_EXIST, false,
                resolveKeyOrDefault(e, request, "auth.userMissing")));
    }

    @ExceptionHandler(FeatureNotEnabledException.class)
    public ResponseEntity<JsonResult> handleFeatureNotEnabledException(FeatureNotEnabledException e, HttpServletRequest request) {
        logger.warn("功能未启用: exceptionType={}", e.getClass().getSimpleName());
        return ResponseEntity.ok(new JsonResult(false,
                resolveExceptionMessage(e, request, "该功能未启用")));
    }

    @ExceptionHandler(RecognitionException.class)
    public ResponseEntity<JsonResult> handleRecognitionException(RecognitionException e, HttpServletRequest request) {
        logger.warn("图像识别异常: exceptionType={}", e.getClass().getSimpleName());
        return ResponseEntity.ok(new JsonResult(false,
                resolveExceptionMessage(e, request, "图像识别服务异常，请稍后重试")));
    }

    // ========== 原 module-specific handlers 合并 ==========

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<JsonResult> handleServerErrorException(ServerErrorException e, HttpServletRequest request) {
        logException("服务端异常：", e);
        String acceptLanguage = acceptLanguage(request);
        String message = hasKey(e) || (e.getMessage() != null && !e.getMessage().isEmpty())
                ? resolveExceptionMessage(e, request, null)
                : Messages.forLanguage("system.busy", acceptLanguage);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonResult(ErrorConstantUtils.INTERNAL_SERVER_ERROR, false, message));
    }

    @ExceptionHandler(ChargeIdempotencyException.class)
    public ResponseEntity<JsonResult> handleChargeIdempotencyException(ChargeIdempotencyException e,
                                                                       HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(e.getCode(), false,
                resolveExceptionMessage(e, request, null)));
    }

    @ExceptionHandler({
            NoAccessException.class, ConfirmedStateException.class, NotAvailableStateException.class,
            NoAccessUpdatingException.class,
            NullIDPhotoException.class, InconsistentAuthenticationException.class,
            AuthenticationRecordExistException.class, IDPhotoCountLimitationException.class,
            IDPhotoSizeLimitationException.class,
            BookRenewOvertimeException.class,
            AmountNotAvailableException.class,
            UserStateErrorException.class, ItemAvailableException.class,
            CountOverLimitException.class, GenerateScheduleException.class,
            NotAvailableConditionException.class,
            RepeatPickException.class, SelfPickException.class,
            DeliveryOrderTakenException.class, SelfTradingOrderException.class,
            DeliveryOrderStateUpdatedException.class,
            NotAvailableTimeException.class,
            CorrectRecordException.class, NoRealNameException.class
    })
    public ResponseEntity<JsonResult> handleBusinessMessageException(Exception e, HttpServletRequest request) {
        return ResponseEntity.ok(new JsonResult(false,
                resolveExceptionMessage(e, request, null)));
    }

    @ExceptionHandler({HourFrequencyLimitException.class, MinuteFrequencyLimitException.class,
            DayFrequencyLimitException.class, IllegalPhoneNumberException.class,
            VerificationCodeInvalidException.class, SendSMSException.class, SendEmailException.class})
    public ResponseEntity<JsonResult> handleVerificationException(Exception e, HttpServletRequest request) {
        logger.warn("VerificationFailure - exceptionType={}", e.getClass().getSimpleName());
        return ResponseEntity.ok(new JsonResult(false,
                resolveExceptionMessage(e, request, null)));
    }

    private String resolveExceptionMessage(Throwable e, HttpServletRequest request, String defaultMessage) {
        String acceptLanguage = acceptLanguage(request);
        if (e instanceof KeyedMessage keyed && hasKey(e)) {
            return Messages.forLanguage(keyed.messageKey(), acceptLanguage, keyed.messageArgs());
        }
        String raw = e != null ? e.getMessage() : null;
        if (raw == null || raw.isEmpty()) {
            raw = defaultMessage;
        }
        return MessageCatalog.localizeMessage(raw, acceptLanguage);
    }

    private String resolveKeyOrDefault(Throwable e, HttpServletRequest request, String defaultKey) {
        String acceptLanguage = acceptLanguage(request);
        if (e instanceof KeyedMessage keyed && hasKey(e)) {
            return Messages.forLanguage(keyed.messageKey(), acceptLanguage, keyed.messageArgs());
        }
        return Messages.forLanguage(defaultKey, acceptLanguage);
    }

    private static boolean hasKey(Throwable e) {
        return e instanceof KeyedMessage keyed
                && keyed.messageKey() != null
                && !keyed.messageKey().isBlank();
    }

    private static String acceptLanguage(HttpServletRequest request) {
        return request != null ? request.getHeader("Accept-Language") : null;
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
                        Messages.forLanguage("system.busy", request.getHeader("Accept-Language"))));
    }
}
