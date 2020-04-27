package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Exception.CommonException.ResourceNotFoundException;
import org.apache.http.MethodNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Order(value = 4)
public class ServletExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(ServletExceptionHandler.class);

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
     * 处理HTTP请求404错误
     *
     * @return
     */
    @ExceptionHandler({NoHandlerFoundException.class, ResourceNotFoundException.class})
    public ModelAndView HandleNoHandlerFoundException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.setViewName("Error/notFoundError");
        return modelAndView;
    }

    /**
     * 处理HTTP请求405错误
     */
    @ExceptionHandler({MethodNotSupportedException.class, HttpRequestMethodNotSupportedException.class})
    public ModelAndView HandleMethodNotSupportedException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
        modelAndView.setViewName("Error/methodNotAllowError");
        return modelAndView;
    }

    /**
     * 处理HTTP请求500错误
     *
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView HandleException(Exception e) {
        logger.error("ServletExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.setViewName("Error/serverError");
        return modelAndView;
    }
}
