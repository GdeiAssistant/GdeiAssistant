package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Account.RestController.CloseAccountRestController;
import edu.gdei.gdeiassistant.Exception.CloseAccountException.ItemAvailableException;
import edu.gdei.gdeiassistant.Exception.CloseAccountException.UserStateErrorException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CloseAccountRestController.class)
@Order(value = 1)
public class CloseAccountRestExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(CloseAccountRestExceptionHandler.class);

    /**
     * 处理用户账号状态异常的错误
     *
     * @return
     */
    @ExceptionHandler(UserStateErrorException.class)
    public ResponseEntity HandleUserStateErrorException(UserStateErrorException e) {
        logger.error("CloseAccountRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "用户账号状态异常，请联系管理员"));
    }

    /**
     * 处理用户账号密码不匹配的异常
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity HandlePasswordIncorrectException(PasswordIncorrectException e) {
        logger.error("CloseAccountRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "用户账号密码不匹配，请重试"));
    }

    /**
     * 处理用户有待处理的社区功能信息的异常
     *
     * @return
     */
    @ExceptionHandler(ItemAvailableException.class)
    public ResponseEntity HandleItemAvailableException(ItemAvailableException e) {
        logger.error("CloseAccountRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, e.getMessage()));
    }

}
