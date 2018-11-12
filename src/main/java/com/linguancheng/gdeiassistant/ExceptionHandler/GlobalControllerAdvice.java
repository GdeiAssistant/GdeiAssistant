package com.linguancheng.gdeiassistant.ExceptionHandler;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice(annotations = Controller.class)
public class GlobalControllerAdvice {

    /**
     * ����HTTP����400����
     *
     * @return
     */
    @ExceptionHandler({MissingServletRequestParameterException.class
            , TypeMismatchException.class})
    public ResponseEntity HandleBadRequestException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    /**
     * ��������У���쳣
     *
     * @return
     */
    @ExceptionHandler({ConstraintViolationException.class
            , MethodArgumentNotValidException.class})
    public ResponseEntity HandleConstraintViolationException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
