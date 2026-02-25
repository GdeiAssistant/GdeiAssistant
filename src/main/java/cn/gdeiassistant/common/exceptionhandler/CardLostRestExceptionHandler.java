package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.core.cardquery.controller.CardController;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CardController.class)
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
