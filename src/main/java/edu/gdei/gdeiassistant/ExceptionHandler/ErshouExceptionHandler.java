package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Ershou.Controller.ErshouController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NoAccessException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NotAvailableStateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackageClasses = ErshouController.class)
@Order(value = 1)
public class ErshouExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(ErshouExceptionHandler.class);

    /**
     * 处理二手交易信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ModelAndView ShowDataNotExistExceptionTip(DataNotExistException e) {
        logger.error("ErshouExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "二手交易信息不存在");
        modelAndView.addObject("ErrorMessage", "查询的二手交易信息不存在");
        return modelAndView;
    }

    /**
     * 处理当前用户没有权限的异常
     *
     * @return
     */
    @ExceptionHandler(NoAccessException.class)
    public ModelAndView ShowNoAccessExceptionTip(NoAccessException e) {
        logger.error("ErshouExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "当前用户没有权限");
        modelAndView.addObject("ErrorMessage", "你没有权限编辑该二手交易信息");
        return modelAndView;
    }

    /**
     * 处理商品已确认售出的异常
     *
     * @return
     */
    @ExceptionHandler(ConfirmedStateException.class)
    public ModelAndView ShowUnmodifiableStateException(ConfirmedStateException e) {
        logger.error("ErshouExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "商品已确认售出");
        modelAndView.addObject("ErrorMessage", "已确认售出的商品不可再次查看和编辑");
        return modelAndView;
    }

    /**
     * 处理商品已下架的异常
     *
     * @return
     */
    @ExceptionHandler(NotAvailableStateException.class)
    public ModelAndView ShowNotAvailableStateException(NotAvailableStateException e) {
        logger.error("ErshouExceptionHandler：", e);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "商品已下架");
        modelAndView.addObject("ErrorMessage", "已下架的商品暂不可查看");
        return modelAndView;
    }

}
