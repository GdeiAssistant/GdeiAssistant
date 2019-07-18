package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.CardQuery.RestController.CardRestController;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CardRestController.class)
@Order(value = 0)
public class CardLostRestExceptionHandler {

    private Log log = LogFactory.getLog(CardLostRestExceptionHandler.class);

    /**
     * 处理校园卡密码错误的异常
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException(PasswordIncorrectException e) {
        log.error("CardLostRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "校园卡密码错误，请检查并重试"));
    }

}
