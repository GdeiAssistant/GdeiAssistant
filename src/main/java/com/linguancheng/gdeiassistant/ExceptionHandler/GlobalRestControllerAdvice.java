package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalRestControllerAdvice {

    /**
     * ����HTTP����400����
     *
     * @return
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, TypeMismatchException.class})
    public JsonResult HandleBadRequestException() {
        return new JsonResult(false, "����������Ϸ�");
    }

    /**
     * ��������У���쳣
     *
     * @return
     */
    @ExceptionHandler({ConstraintViolationException.class
            , MethodArgumentNotValidException.class})
    public JsonResult HandleConstraintViolationException() {
        return new JsonResult(false, "����������Ϸ�");
    }


}
