package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Ershou.ErshouController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NoAccessException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NotAvailableStateException;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = ErshouController.class)
@Order(Integer.MIN_VALUE)
public class ErshouExceptionHandler {

    @ExceptionHandler(DataNotExistException.class)
    public ModelAndView ShowDataNotExistExceptionTip() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "二手交易信息不存在");
        modelAndView.addObject("ErrorMessage", "查询的二手交易信息不存在");
        return modelAndView;
    }

    @ExceptionHandler(NoAccessException.class)
    public ModelAndView ShowNoAccessExceptionTip() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "当前用户没有权限");
        modelAndView.addObject("ErrorMessage", "你没有权限编辑该二手交易信息");
        return modelAndView;
    }

    @ExceptionHandler(ConfirmedStateException.class)
    public ModelAndView ShowUnmodifiableStateException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "商品已确认售出");
        modelAndView.addObject("ErrorMessage", "已确认售出的商品不可再次查看和编辑");
        return modelAndView;
    }

    @ExceptionHandler(NotAvailableStateException.class)
    public ModelAndView ShowNotAvailableStateException() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "商品已下架");
        modelAndView.addObject("ErrorMessage", "已下架的商品暂不可查看");
        return modelAndView;
    }

}
