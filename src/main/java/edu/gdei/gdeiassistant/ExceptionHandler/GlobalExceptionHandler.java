package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.MethodNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.ConstraintViolationException;
import java.io.IOException;

@ControllerAdvice(annotations = Controller.class)
@Order(value = 3)
public class GlobalExceptionHandler {

    private Log log = LogFactory.getLog(GlobalExceptionHandler.class);

    /**
     * 处理HTTP请求400错误
     *
     * @return
     */
    @ExceptionHandler({MissingServletRequestParameterException.class
            , TypeMismatchException.class, HttpMessageNotReadableException.class})
    public ModelAndView HandleBadRequestException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.setViewName("Error/badRequestError");
        return modelAndView;
    }

    /**
     * 处理HTTP请求405错误
     *
     * @throws IOException
     */
    @ExceptionHandler(MethodNotSupportedException.class)
    public ModelAndView HandleMethodNotSupportedException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
        modelAndView.setViewName("Error/methodNotAllowError");
        return modelAndView;
    }

    /**
     * 处理数据校验异常
     *
     * @return
     */
    @ExceptionHandler({ConstraintViolationException.class
            , MethodArgumentNotValidException.class})
    public ModelAndView HandleConstraintViolationException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.setViewName("Error/badRequestError");
        return modelAndView;
    }

    /**
     * 处理查询数据不存在的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ModelAndView HandleDataNotExistException(DataNotExistException e) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "查询的数据不存在");
        modelAndView.addObject("ErrorMessage", e.getMessage());
        return modelAndView;
    }

    /**
     * 处理系统异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView HandleException(Exception e) {
        log.error(e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "服务暂不可用");
        modelAndView.addObject("ErrorMessage", "系统异常，请联系管理员");
        return modelAndView;
    }
}
