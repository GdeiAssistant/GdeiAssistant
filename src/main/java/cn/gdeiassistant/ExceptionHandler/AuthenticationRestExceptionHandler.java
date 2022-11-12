package cn.gdeiassistant.ExceptionHandler;

import cn.gdeiassistant.Controller.Authentication.RestController.AuthenticationRestController;
import cn.gdeiassistant.Exception.AuthenticationException.AuthenticationRecordExistException;
import cn.gdeiassistant.Exception.AuthenticationException.InconsistentAuthenticationException;
import cn.gdeiassistant.Exception.AuthenticationException.NullIDPhotoException;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthenticationRestController.class)
@Order(value = 0)
public class AuthenticationRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationRestExceptionHandler.class);

    /**
     * 处理实名认证时非中国居民身份证且未上传证件照片的异常
     *
     * @return
     */
    @ExceptionHandler(NullIDPhotoException.class)
    public ResponseEntity HandleNullIDPhotoExceptionException(NullIDPhotoException e) {
        logger.error("AuthenticationRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "证件照片不能为空"));
    }

    /**
     * 处理实名认证时中国居民身份证无法通过校验的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(InconsistentAuthenticationException.class)
    public ResponseEntity HandleInconsistentAuthenticationException(InconsistentAuthenticationException e) {
        logger.error("InconsistentAuthenticationException：", e);
        return ResponseEntity.ok(new JsonResult(false, "身份证校验不通过"));
    }

    /**
     * 处理实名认证已存在记录的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(AuthenticationRecordExistException.class)
    public ResponseEntity HandleAuthenticationRecordExistException(AuthenticationRecordExistException e) {
        logger.error("AuthenticationRecordExistException：", e);
        return ResponseEntity.ok(new JsonResult(false, "已存在实名认证信息，请先取消实名认证再进行操作"));
    }
}
