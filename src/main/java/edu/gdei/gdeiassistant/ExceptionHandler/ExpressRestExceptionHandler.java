package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Express.RestController.ExpressRestController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.ExpressException.CorrectRecordException;
import edu.gdei.gdeiassistant.Exception.ExpressException.NoRealNameException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ExpressRestController.class)
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
