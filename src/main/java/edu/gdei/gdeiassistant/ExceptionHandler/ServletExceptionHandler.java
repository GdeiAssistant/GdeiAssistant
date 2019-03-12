package edu.gdei.gdeiassistant.ExceptionHandler;

import org.apache.http.MethodNotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class ServletExceptionHandler {

    /**
     * 处理HTTP请求404错误
     *
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView HandleNoHandlerFoundException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.setViewName("Error/notFoundError");
        return modelAndView;
    }

    /**
     * 处理HTTP请求405错误
     */
    @ExceptionHandler(MethodNotSupportedException.class)
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
    public ModelAndView HandleException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.setViewName("Error/serverError");
        return modelAndView;
    }


}
