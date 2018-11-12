package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Constant.ConstantUtils;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.apache.http.MethodNotSupportedException;
import org.springframework.beans.TypeMismatchException;
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
public class GlobalRestControllerExceptionHandler {

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity HandleException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JsonResult(false, "系统异常，请联系管理员"));
    }


}
