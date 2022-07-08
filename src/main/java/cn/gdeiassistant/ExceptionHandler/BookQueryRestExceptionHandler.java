package cn.gdeiassistant.ExceptionHandler;

import cn.gdeiassistant.Controller.AcademicAffairs.BookQuery.RestController.BookQueryRestController;
import cn.gdeiassistant.Exception.BookRenewException.BookRenewOvertimeException;
import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = BookQueryRestController.class)
@Order(value = 0)
public class BookQueryRestExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(BookQueryRestExceptionHandler.class);

    /**
     * 处理图书续借超过次数限制的异常
     *
     * @return
     */
    @ExceptionHandler(BookRenewOvertimeException.class)
    public ResponseEntity HandleBookRenewOvertimeException(BookRenewOvertimeException e) {
        logger.error("BookQueryRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "图书续借超过次数限制"));
    }

    /**
     * 处理图书馆查询密码错误
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException(PasswordIncorrectException e) {
        logger.error("BookQueryRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "图书馆查询密码错误，请检查并重试"));
    }
}
