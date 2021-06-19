package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Secret.SecretController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = SecretController.class)
@Order(value = 1)
public class SecretExceptionHandler {

    private Log log = LogFactory.getLog(SecretExceptionHandler.class);

    /**
     * 处理树洞信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ModelAndView ShowDataNotExistExceptionTip(DataNotExistException e) {
        log.error("SecretExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "树洞信息不存在");
        modelAndView.addObject("ErrorMessage", "查询的树洞信息不存在");
        return modelAndView;
    }
}
