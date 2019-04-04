package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.LostAndFound.LostAndFoundRestController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NoAccessException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = LostAndFoundRestController.class)
@Order(Integer.MIN_VALUE)
public class LostAndFoundRestExceptionHandler {

    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDataNotExistExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "查询的失物招领信息不存在"));
    }

    @ExceptionHandler(NoAccessException.class)
    public ResponseEntity ShowNoAccessExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "你没有权限编辑该失物招领信息"));
    }

    @ExceptionHandler(ConfirmedStateException.class)
    public ResponseEntity ShowUnmodifiableStateExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false
                , "该失物招领信息已确认寻回，不能再次编辑和查看"));
    }
}
