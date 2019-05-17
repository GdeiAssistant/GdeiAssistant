package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Secret.SecretRestController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = SecretRestController.class)
@Order(value = 0)
public class SecretRestExceptionHandler {

    private Log log = LogFactory.getLog(SecretRestExceptionHandler.class);

    /**
     * 处理校园树洞信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDataNotExistExceptionTip(DataNotExistException e) {
        log.error(e);
        return ResponseEntity.ok(new JsonResult(false
                , "查询的校园树洞信息不存在"));
    }
}
