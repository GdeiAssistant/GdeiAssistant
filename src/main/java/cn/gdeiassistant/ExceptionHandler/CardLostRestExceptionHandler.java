package cn.gdeiassistant.ExceptionHandler;

import cn.gdeiassistant.Controller.CardQuery.RestController.CardRestController;
import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CardRestController.class)
@Order(value = 0)
public class CardLostRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(CardLostRestExceptionHandler.class);

    /**
     * 处理校园卡密码错误的异常
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException(PasswordIncorrectException e) {
        logger.error("CardLostRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "校园卡密码错误，请检查并重试"));
    }

}
