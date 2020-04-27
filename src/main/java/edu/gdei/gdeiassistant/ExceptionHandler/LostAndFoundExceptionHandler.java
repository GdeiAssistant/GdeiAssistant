package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.LostAndFound.Controller.LostAndFoundController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NoAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = LostAndFoundController.class)
@Order(value = 1)
public class LostAndFoundExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(LostAndFoundExceptionHandler.class);

    /**
     * 处理失物招领信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ModelAndView ShowDataNotExistExceptionTip(DataNotExistException e) {
        logger.error("LostAndFoundExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "失物招领信息不存在");
        modelAndView.addObject("ErrorMessage", "查询的失物招领信息不存在");
        return modelAndView;
    }

    /**
     * 处理用户没有权限编辑的异常
     *
     * @return
     */
    @ExceptionHandler(NoAccessException.class)
    public ModelAndView ShowNoAccessExceptionTip(NoAccessException e) {
        logger.error("LostAndFoundExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "当前用户没有权限");
        modelAndView.addObject("ErrorMessage", "你没有权限编辑该失物招领信息");
        return modelAndView;
    }

    /**
     * 处理物品已确认寻回的异常
     *
     * @return
     */
    @ExceptionHandler(ConfirmedStateException.class)
    public ModelAndView ShowUnmodifiableStateException(ConfirmedStateException e) {
        logger.error("LostAndFoundExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "物品已确认寻回");
        modelAndView.addObject("ErrorMessage", "该失物招领信息已确认寻回，不可再次查看和编辑");
        return modelAndView;
    }
}
