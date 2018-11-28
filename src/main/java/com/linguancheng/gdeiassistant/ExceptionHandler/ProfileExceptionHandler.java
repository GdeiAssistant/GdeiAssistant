package com.gdeiassistant.gdeiassistant.ExceptionHandler;

import com.gdeiassistant.gdeiassistant.Controller.UserProfile.ProfileController;
import com.gdeiassistant.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = ProfileController.class)
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
