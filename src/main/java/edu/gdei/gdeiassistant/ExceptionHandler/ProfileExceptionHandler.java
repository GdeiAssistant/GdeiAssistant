package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.UserProfile.ProfileController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = ProfileController.class)
@Order(Integer.MIN_VALUE)
public class ProfileExceptionHandler {

    @ExceptionHandler(DataNotExistException.class)
    public ModelAndView ShowDataNotExistExceptionTip() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "用户不存在");
        modelAndView.addObject("ErrorMessage", "该用户不存在或已自主注销");
        return modelAndView;
    }
}
