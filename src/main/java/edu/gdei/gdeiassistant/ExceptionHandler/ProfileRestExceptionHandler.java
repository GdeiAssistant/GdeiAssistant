package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.UserProfile.RestController.ProfileRestController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackageClasses = ProfileRestController.class)
@Order(value = 0)
public class ProfileRestExceptionHandler {

    /**
     * 处理个人资料昵称重复的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDuplicateKeyExceptionTip(DuplicateKeyException e) {
        return ResponseEntity.ok(new JsonResult(false
                , "该昵称已存在"));
    }
}