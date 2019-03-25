package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.BookQuery.BookQueryRestController;
import edu.gdei.gdeiassistant.Exception.BookRenewException.BookRenewOvertimeException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = BookQueryRestController.class)
@Order(Integer.MIN_VALUE)
public class BookQueryExceptionHandler {

    @ExceptionHandler(BookRenewOvertimeException.class)
    public ResponseEntity HandleBookRenewOvertimeException() {
        return ResponseEntity.ok(new JsonResult(false, "图书续借超过次数限制"));
    }

    /**
     * 处理图书馆查询密码错误
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException() {
        return ResponseEntity.ok(new JsonResult(false, "图书馆查询密码错误，请检查并重试"));
    }
}
