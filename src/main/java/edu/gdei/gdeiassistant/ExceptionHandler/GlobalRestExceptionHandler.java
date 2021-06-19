package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ConstantUtils;
import edu.gdei.gdeiassistant.Exception.ChargeException.SecurityInvalidException;
import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import edu.gdei.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import edu.gdei.gdeiassistant.Exception.QueryException.TimeStampIncorrectException;
import edu.gdei.gdeiassistant.Exception.UserLoginException.UserGraduatedException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.MethodNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice(annotations = RestController.class)
@Order(value = 2)
public class GlobalRestExceptionHandler {

    private Log log = LogFactory.getLog(GlobalRestExceptionHandler.class);

    /**
     * 处理HTTP请求400错误
     *
     * @return
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, TypeMismatchException.class
            , HttpMessageNotReadableException.class})
    public ResponseEntity HandleBadRequestException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.INCORRECT_REQUEST_PARAM, false
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
        return ResponseEntity.ok(new JsonResult(ConstantUtils.INCORRECT_REQUEST_PARAM, false
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
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.DATA_NOT_EXIST, false, e.getMessage()));
    }

    /**
     * 处理网络连接超时异常
     *
     * @return
     */
    @ExceptionHandler(NetWorkTimeoutException.class)
    public ResponseEntity HandleNetWorkTimeoutException(NetWorkTimeoutException e) {
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.NETWORK_TIMEOUT, false, "网络连接超时，请重试"));
    }

    /**
     * 处理查询条件不合法的异常
     *
     * @return
     */
    @ExceptionHandler(ErrorQueryConditionException.class)
    public ResponseEntity HandleErrorQueryConditionException(ErrorQueryConditionException e) {
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ERROR_QUERY_CONDITION, false, "查询条件不合法，请重新填写"));
    }

    /**
     * 处理时间戳校验失败的异常
     *
     * @return
     */
    @ExceptionHandler(TimeStampIncorrectException.class)
    public ResponseEntity HandleTimeStampIncorrectException(TimeStampIncorrectException e) {
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.TIMESTAMP_INVALIDATED, false, "时间戳校验失败，请尝试重新登录"));
    }

    /**
     * 处理安全校验不通过异常
     *
     * @return
     */
    @ExceptionHandler(SecurityInvalidException.class)
    public ResponseEntity HandleSecurityInvalidException(SecurityInvalidException e) {
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.CHARGE_SECURITY_INVALID
                , false, "充值安全校验不通过"));
    }

    /**
     * 处理用户账号密码错误的异常
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException(PasswordIncorrectException e) {
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.PASSWORD_INCORRECT
                , false, "用户账号密码错误，请检查重试或重新登录"));
    }

    /**
     * 处理用户不存在的异常
     *
     * @return
     */
    @ExceptionHandler(UserNotExistException.class)
    public ResponseEntity HandleUserNotExistException(UserNotExistException e) {
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.USER_NOT_EXIST
                , false, "当前用户不存在，请尝试重新登录"));
    }

    /**
     * 处理用户账号已毕业注销的异常
     *
     * @return
     */
    @ExceptionHandler(UserGraduatedException.class)
    public ResponseEntity HandleUserGraduatedException(UserGraduatedException e) {
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ACCOUNT_GRADUATED
                , false, "账号为毕业用户账号，无法使用教务查询功能"));
    }

    /**
     * 处理系统异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity HandleException(Exception e) {
        log.error("GlobalRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ConstantUtils.INTERNAL_SERVER_ERROR, false, "系统异常，请联系管理员"));
    }
}
