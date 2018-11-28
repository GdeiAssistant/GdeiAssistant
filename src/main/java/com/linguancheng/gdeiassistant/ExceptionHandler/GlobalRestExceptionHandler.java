package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Constant.ConstantUtils;
import com.linguancheng.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.MethodNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice(annotations = RestController.class)
@Order(value = Integer.MIN_VALUE)
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JsonResult(ConstantUtils.INCORRECT_REQUEST_PARAM, false
                        , "请求参数不合法"));
    }

    /**
     * 处理HTTP请求405错误
     *
     * @return
     */
    @ExceptionHandler(MethodNotSupportedException.class)
    public ResponseEntity HandleMethodNotSupportedException() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new JsonResult(false, "请求方法不支持"));
    }

    /**
     * 处理数据校验异常
     *
     * @return
     */
    @ExceptionHandler({ConstraintViolationException.class
            , MethodArgumentNotValidException.class})
    public ResponseEntity HandleConstraintViolationException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new JsonResult(ConstantUtils.INCORRECT_REQUEST_PARAM, false
                        , "请求参数不合法"));
    }

    /**
     * 处理网络连接超时异常
     *
     * @return
     */
    @ExceptionHandler(NetWorkTimeoutException.class)
    public ResponseEntity HandleNetWorkTimeoutException() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new JsonResult(ConstantUtils.NETWORK_TIMEOUT, false, "网络连接超时，请重试"));
    }

    /**
     * 处理系统异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity HandleException(Exception e) {
        log.error("系统异常：", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonResult(ConstantUtils.INTERNAL_SERVER_ERROR, false, "系统异常，请联系管理员"));
    }


}
