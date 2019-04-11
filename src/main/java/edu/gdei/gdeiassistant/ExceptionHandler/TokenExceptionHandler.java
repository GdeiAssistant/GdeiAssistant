package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ConstantUtils;
import edu.gdei.gdeiassistant.Exception.TokenValidException.TokenExpiredException;
import edu.gdei.gdeiassistant.Exception.TokenValidException.TokenNotMatchingException;
import edu.gdei.gdeiassistant.Exception.TokenValidException.TokenServerException;
import edu.gdei.gdeiassistant.Exception.TokenValidException.UnusualLocationException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = {RestController.class, Aspect.class})
@Order(value = 0)
public class TokenExceptionHandler {

    /**
     * 处理登录凭证已过期的异常
     *
     * @return
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity HandleTokenExpiredException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.TOKEN_EXPIRED_EXCEPTION
                , false, "登录凭证已过期，请重新登录"));
    }

    /**
     * 处理登录地点的异常
     *
     * @return
     */
    @ExceptionHandler(UnusualLocationException.class)
    public ResponseEntity HandleUnusualLocationException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.UNUSUAL_LOCATION_EXCEPTION
                , false, "登录地点异常，请进行身份验证"));
    }

    /**
     * 处理没有登录凭证记录的异常
     *
     * @return
     */
    @ExceptionHandler(TokenNotMatchingException.class)
    public ResponseEntity HandleTokenNotMatchingException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.TOKEN_NOT_MATCHING
                , false, "没有对应的登录凭证记录，请尝试重新登录"));
    }

    /**
     * 处理登录凭证校验服务异常的错误
     *
     * @return
     */
    @ExceptionHandler(TokenServerException.class)
    public ResponseEntity HandleTokenServerException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.TOKEN_SERVER_ERROR
                , false, "登录凭证校验服务异常，请联系管理员"));
    }
}
