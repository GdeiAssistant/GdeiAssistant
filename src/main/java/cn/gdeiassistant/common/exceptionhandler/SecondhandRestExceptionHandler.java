package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.core.secondhand.controller.SecondhandController;
import cn.gdeiassistant.common.exception.DatabaseException.ConfirmedStateException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.exception.DatabaseException.NotAvailableStateException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = SecondhandController.class)
@Order(value = 0)
public class SecondhandRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(SecondhandRestExceptionHandler.class);

    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity<?> showDataNotExistExceptionTip(DataNotExistException e) {
        logger.error("SecondhandRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "查询的二手交易信息不存在"));
    }

    @ExceptionHandler(NoAccessException.class)
    public ResponseEntity<?> showNoAccessExceptionTip(NoAccessException e) {
        logger.error("SecondhandRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "你没有权限编辑该二手交易信息"));
    }

    @ExceptionHandler(ConfirmedStateException.class)
    public ResponseEntity<?> showUnmodifiableStateExceptionTip(ConfirmedStateException e) {
        logger.error("SecondhandRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "已确认售出的商品不能再次编辑和查看"));
    }

    @ExceptionHandler(NotAvailableStateException.class)
    public ResponseEntity<?> showNotAvailableStateExceptionTip(NotAvailableStateException e) {
        logger.error("SecondhandRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "已下架的商品暂不能查看"));
    }
}
