package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.LostAndFound.LostAndFoundController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NoAccessException;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = LostAndFoundController.class)
@Order(Integer.MIN_VALUE)
public class LostAndFoundExceptionHandler {

    @ExceptionHandler(DataNotExistException.class)
    public ModelAndView ShowDataNotExistExceptionTip() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "失物招领信息不存在");
        modelAndView.addObject("ErrorMessage", "查询的失物招领信息不存在");
        return modelAndView;
    }

    @ExceptionHandler(NoAccessException.class)
    public ModelAndView ShowNoAccessExceptionTip() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "当前用户没有权限");
        modelAndView.addObject("ErrorMessage", "你没有权限编辑该失物招领信息");
        return modelAndView;
    }

    @ExceptionHandler(ConfirmedStateException.class)
    public ModelAndView ShowUnmodifiableStateException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "物品已确认寻回");
        modelAndView.addObject("ErrorMessage", "该失物招领信息已确认寻回，不可再次查看和编辑");
        return modelAndView;
    }
}
