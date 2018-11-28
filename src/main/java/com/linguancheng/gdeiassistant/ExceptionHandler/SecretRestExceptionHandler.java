package com.gdeiassistant.gdeiassistant.ExceptionHandler;

import com.gdeiassistant.gdeiassistant.Controller.Secret.SecretRestController;
import com.gdeiassistant.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.gdeiassistant.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = SecretRestController.class)
public class SecretRestExceptionHandler {

    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDataNotExistExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "查询的校园树洞信息不存在"));
    }
}
