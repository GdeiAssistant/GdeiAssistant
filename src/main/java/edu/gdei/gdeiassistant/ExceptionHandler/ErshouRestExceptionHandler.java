package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Ershou.RestController.ErshouRestController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NoAccessException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NotAvailableStateException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ErshouRestController.class)
@Order(value = 0)
public class ErshouRestExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(ErshouRestExceptionHandler.class);

    /**
     * 处理二手交易信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDataNotExistExceptionTip(DataNotExistException e) {
        logger.error("ErshouRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "查询的二手交易信息不存在"));
    }

    /**
     * 处理没有权限编辑二手交易信息的异常
     *
     * @return
     */
    @ExceptionHandler(NoAccessException.class)
    public ResponseEntity ShowNoAccessExceptionTip(NoAccessException e) {
        logger.error("ErshouRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "你没有权限编辑该二手交易信息"));
    }

    /**
     * 处理商品已确认售出的异常
     *
     * @return
     */
    @ExceptionHandler(ConfirmedStateException.class)
    public ResponseEntity ShowUnmodifiableStateExceptionTip(ConfirmedStateException e) {
        logger.error("ErshouRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "已确认售出的商品不能再次编辑和查看"));
    }

    /**
     * 处理商品已下架的异常
     *
     * @return
     */
    @ExceptionHandler(NotAvailableStateException.class)
    public ResponseEntity ShowNotAvailableStateExceptionTip(NotAvailableStateException e) {
        logger.error("ErshouRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "已下架的商品暂不能查看"));
    }
}
