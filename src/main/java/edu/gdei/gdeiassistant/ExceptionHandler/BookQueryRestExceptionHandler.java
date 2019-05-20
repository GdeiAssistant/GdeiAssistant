package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.BookQuery.BookQueryRestController;
import edu.gdei.gdeiassistant.Exception.BookRenewException.BookRenewOvertimeException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = BookQueryRestController.class)
@Order(value = 0)
public class BookQueryRestExceptionHandler {

    private Log log = LogFactory.getLog(BookQueryRestExceptionHandler.class);

    /**
     * 处理图书续借超过次数限制的异常
     *
     * @return
     */
    @ExceptionHandler(BookRenewOvertimeException.class)
    public ResponseEntity HandleBookRenewOvertimeException(BookRenewOvertimeException e) {
        log.error("BookQueryRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "图书续借超过次数限制"));
    }

    /**
     * 处理图书馆查询密码错误
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException(PasswordIncorrectException e) {
        log.error("BookQueryRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "图书馆查询密码错误，请检查并重试"));
    }
}
