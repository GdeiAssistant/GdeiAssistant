package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.ConstraintViolationException;

@ControllerAdvice(annotations = Controller.class)
@Order(value = 3)
public class GlobalExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        logger.error("GlobalExceptionHandler：", e);
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
        logger.error("GlobalExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "服务暂不可用");
        modelAndView.addObject("ErrorMessage", "系统异常，请联系管理员");
        return modelAndView;
    }
}
