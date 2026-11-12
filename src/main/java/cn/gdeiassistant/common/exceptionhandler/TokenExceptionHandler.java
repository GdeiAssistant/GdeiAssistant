package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.tokenvalidexception.SuspiciouseRequestException;
import cn.gdeiassistant.common.exception.tokenvalidexception.TokenExpiredException;
import cn.gdeiassistant.common.exception.tokenvalidexception.TokenNotMatchingException;
import cn.gdeiassistant.common.exception.tokenvalidexception.TokenServerException;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.common.i18n.MessageCatalog;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import cn.gdeiassistant.common.i18n.Messages;

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
    public ResponseEntity<JsonResult> handleTokenExpiredException(TokenExpiredException e, HttpServletRequest request) {
        logger.error("TokenExceptionHandler：", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new JsonResult(ErrorConstantUtils.TOKEN_EXPIRED_EXCEPTION, false,
                        Messages.forLanguage("token.expired", request.getHeader("Accept-Language"))));
    }

    /**
     * 处理可疑访问请求的异常
     *
     * @return
     */
    @ExceptionHandler(SuspiciouseRequestException.class)
    public ResponseEntity<JsonResult> handleUnusualLocationException(SuspiciouseRequestException e, HttpServletRequest request) {
        logger.error("TokenExceptionHandler：", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new JsonResult(ErrorConstantUtils.UNUSUAL_LOCATION_EXCEPTION, false,
                        Messages.forLanguage("token.suspicious", request.getHeader("Accept-Language"))));
    }

    /**
     * 处理没有登录凭证记录的异常
     *
     * @return
     */
    @ExceptionHandler(TokenNotMatchingException.class)
    public ResponseEntity<JsonResult> handleTokenNotMatchingException(TokenNotMatchingException e, HttpServletRequest request) {
        logger.error("TokenExceptionHandler：", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new JsonResult(ErrorConstantUtils.TOKEN_NOT_MATCHING, false,
                        Messages.forLanguage("token.notFound", request.getHeader("Accept-Language"))));
    }

    /**
     * 处理登录凭证校验服务异常的错误
     *
     * @return
     */
    @ExceptionHandler(TokenServerException.class)
    public ResponseEntity<JsonResult> handleTokenServerException(TokenServerException e, HttpServletRequest request) {
        logger.error("TokenExceptionHandler：", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new JsonResult(ErrorConstantUtils.TOKEN_SERVER_ERROR, false,
                        Messages.forLanguage("token.serviceError", request.getHeader("Accept-Language"))));
    }
}
