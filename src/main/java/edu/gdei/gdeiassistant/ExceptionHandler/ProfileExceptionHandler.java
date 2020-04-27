package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.UserProfile.Controller.ProfileController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = ProfileController.class)
@Order(value = 1)
public class ProfileExceptionHandler {

    private Logger logger= LoggerFactory.getLogger(ProfileExceptionHandler.class);

    /**
     * 处理用户资料不存在的异常
     *
     * @return
     */
    @ExceptionHandler(UserNotExistException.class)
    public ModelAndView ShowDataNotExistExceptionTip(UserNotExistException e) {
        logger.error("ProfileExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "用户不存在");
        modelAndView.addObject("ErrorMessage", "该用户不存在或已自主注销");
        return modelAndView;
    }
}
