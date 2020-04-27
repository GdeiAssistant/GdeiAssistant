package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ErrorConstantUtils;
import edu.gdei.gdeiassistant.Controller.UserLogin.RestController.UserLoginRestController;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Exception.RequestValidException.NonceInvalidException;
import edu.gdei.gdeiassistant.Exception.RequestValidException.SignInvalidException;
import edu.gdei.gdeiassistant.Exception.RequestValidException.TimestampInvalidException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserLoginRestController.class)
@Order(value = 0)
public class UserLoginRestExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(UserLoginRestExceptionHandler.class);

    /**
     * 处理账户密码错误异常
     *
     * @return
     */
    @ExceptionHandler(PasswordIncorrectException.class)
    public ResponseEntity ShowPasswordIncorrectExceptionTip(PasswordIncorrectException e) {
        logger.error("UserLoginRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "用户名或密码错误，请重新输入"));
    }

    /**
     * 处理请求摘要签名校验失败异常
     *
     * @return
     */
    @ExceptionHandler(SignInvalidException.class)
    public ResponseEntity ShowSignInvalidExceptionTip(SignInvalidException e) {
        logger.error("UserLoginRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.REQUEST_SIGN_INVALID
                , false, "请求的摘要签名不匹配"));
    }

    /**
     * 处理请求时间戳校验失败异常
     *
     * @return
     */
    @ExceptionHandler(TimestampInvalidException.class)
    public ResponseEntity ShowTimestampInvalidExceptionTip(TimestampInvalidException e) {
        logger.error("UserLoginRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.REQUEST_TIMESTAMP_INVALID
                , false, "请求时间戳校验失败"));
    }

    /**
     * 处理请求随机值重复异常
     *
     * @return
     */
    @ExceptionHandler(NonceInvalidException.class)
    public ResponseEntity ShowNonceInvalidExceptionTip(NonceInvalidException e) {
        logger.error("UserLoginRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.REPLAY_REQUEST
                , false, "重复提交的请求"));
    }
}
