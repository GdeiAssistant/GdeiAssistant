package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.core.express.controller.ExpressController;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.ExpressException.CorrectRecordException;
import cn.gdeiassistant.common.exception.ExpressException.NoRealNameException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ExpressController.class)
@Order(value = 0)
public class ExpressRestExceptionHandler {

    /**
     * 已经存在正确的猜一下记录时，抛出该异常
     *
     * @return
     */
    @ExceptionHandler(CorrectRecordException.class)
    public ResponseEntity HandleCorrectRecordException() {
        return ResponseEntity.ok(new JsonResult(false, "你已经猜中过真实姓名了"));
    }

    /**
     * 处理表白信息没有填写真实姓名的异常
     *
     * @return
     */
    @ExceptionHandler(NoRealNameException.class)
    public ResponseEntity HandleNoRealNameException() {
        return ResponseEntity.ok(new JsonResult(false, "该表白信息没有填写真实姓名"));
    }

    /**
     * 处理表白信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity HandleDataNotExistException() {
        return ResponseEntity.ok(new JsonResult(false, "该表白信息不存在"));
    }
}
