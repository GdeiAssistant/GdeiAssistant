package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.Ershou.RestController.ErshouRestController;
import edu.gdei.gdeiassistant.Controller.Topic.RestController.TopicRestController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = TopicRestController.class)
@Order(value = 0)
public class TopicRestExceptionHandler {

    /**
     * 处理话题信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity HandleDataNotExistException() {
        return ResponseEntity.ok(new JsonResult(false, "该话题信息不存在"));
    }
}
