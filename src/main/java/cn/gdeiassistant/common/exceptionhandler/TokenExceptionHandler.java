package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.TokenValidException.SuspiciouseRequestException;
import cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException;
import cn.gdeiassistant.common.exception.TokenValidException.TokenNotMatchingException;
import cn.gdeiassistant.common.exception.TokenValidException.TokenServerException;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = {RestController.class, Aspect.class})
@Order(value = 0)
public class TokenExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(TokenExceptionHandler.class);

    /**
     * 处理登录凭证已过期的异常
     *
     * @return
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<JsonResult> HandleTokenExpiredException(TokenExpiredException e) {
        logger.error("TokenExceptionHandler：", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new JsonResult(ErrorConstantUtils.TOKEN_EXPIRED_EXCEPTION, false, "登录凭证已过期，请重新登录"));
    }

    /**
     * 处理可疑访问请求的异常
     *
     * @return
     */
    @ExceptionHandler(SuspiciouseRequestException.class)
    public ResponseEntity<JsonResult> HandleUnusualLocationException(SuspiciouseRequestException e) {
        logger.error("TokenExceptionHandler：", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new JsonResult(ErrorConstantUtils.UNUSUAL_LOCATION_EXCEPTION, false, "可疑的登录请求，请退出账号并重新登录进行身份验证"));
    }

    /**
     * 处理没有登录凭证记录的异常
     *
     * @return
     */
    @ExceptionHandler(TokenNotMatchingException.class)
    public ResponseEntity<JsonResult> HandleTokenNotMatchingException(TokenNotMatchingException e) {
        logger.error("TokenExceptionHandler：", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new JsonResult(ErrorConstantUtils.TOKEN_NOT_MATCHING, false, "没有对应的登录凭证记录，请尝试重新登录"));
    }

    /**
     * 处理登录凭证校验服务异常的错误
     *
     * @return
     */
    @ExceptionHandler(TokenServerException.class)
    public ResponseEntity<JsonResult> HandleTokenServerException(TokenServerException e) {
        logger.error("TokenExceptionHandler：", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new JsonResult(ErrorConstantUtils.TOKEN_SERVER_ERROR, false, "登录凭证校验服务异常，请联系管理员"));
    }
}
