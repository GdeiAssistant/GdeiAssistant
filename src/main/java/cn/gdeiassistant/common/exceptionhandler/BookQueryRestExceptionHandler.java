package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.core.bookquery.controller.BookQueryController;
import cn.gdeiassistant.common.exception.BookRenewException.BookRenewOvertimeException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = BookQueryController.class)
@Order(value = 0)
public class BookQueryRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(BookQueryRestExceptionHandler.class);

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
