package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Account.RestController.PasswordRestController;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = PasswordRestController.class)
@Order(value = 1)
public class PasswordRestExceptionHandler {

    private Logger logger= LoggerFactory.getLogger(PasswordRestExceptionHandler.class);

    /**
     * 处理用户账号旧密码不匹配的错误
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException(PasswordIncorrectException e) {
        logger.error("PasswordRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "用户账号旧密码不匹配，请重新尝试"));
    }

}
