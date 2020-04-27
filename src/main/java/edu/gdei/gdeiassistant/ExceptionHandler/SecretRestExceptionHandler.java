package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Secret.RestController.SecretRestController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = SecretRestController.class)
@Order(value = 0)
public class SecretRestExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(SecretRestExceptionHandler.class);

    /**
     * 处理校园树洞信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDataNotExistExceptionTip(DataNotExistException e) {
        logger.error("SecretRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false
                , "查询的校园树洞信息不存在"));
    }
}
