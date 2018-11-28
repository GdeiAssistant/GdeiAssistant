package com.gdeiassistant.gdeiassistant.ExceptionHandler;

import com.gdeiassistant.gdeiassistant.Controller.UserProfile.ProfileRestController;
import com.gdeiassistant.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.gdeiassistant.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ProfileRestController.class)
public class ProfileRestExceptionHandler {

    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDataNotExistExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "查询的用户不存在，请尝试重新登录"));
    }
}
