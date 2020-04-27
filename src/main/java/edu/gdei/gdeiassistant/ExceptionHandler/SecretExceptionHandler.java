package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Secret.Controller.SecretController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = SecretController.class)
@Order(value = 1)
public class SecretExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(SecretExceptionHandler.class);

    /**
     * 处理树洞信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ModelAndView ShowDataNotExistExceptionTip(DataNotExistException e) {
        logger.error("SecretExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "树洞信息不存在");
        modelAndView.addObject("ErrorMessage", "查询的树洞信息不存在");
        return modelAndView;
    }
}
