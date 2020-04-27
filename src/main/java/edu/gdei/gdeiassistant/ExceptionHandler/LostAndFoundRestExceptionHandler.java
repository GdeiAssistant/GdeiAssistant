package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Controller.LostAndFound.RestController.LostAndFoundRestController;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.NoAccessException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = LostAndFoundRestController.class)
@Order(value = 0)
public class LostAndFoundRestExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(LostAndFoundRestExceptionHandler.class);

    /**
     * 处理失物招领信息不存在的异常
     *
     * @return
     */
    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDataNotExistExceptionTip(DataNotExistException e) {
        logger.error("LostAndFoundRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false
                , "查询的失物招领信息不存在"));
    }

    /**
     * 处理用户没有权限编辑的异常
     *
     * @return
     */
    @ExceptionHandler(NoAccessException.class)
    public ResponseEntity ShowNoAccessExceptionTip(NoAccessException e) {
        logger.error("LostAndFoundRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false
                , "你没有权限编辑该失物招领信息"));
    }

    /**
     * 处理失物招领信息已确认寻回的异常
     *
     * @return
     */
    @ExceptionHandler(ConfirmedStateException.class)
    public ResponseEntity ShowUnmodifiableStateExceptionTip(ConfirmedStateException e) {
        logger.error("LostAndFoundRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false
                , "该失物招领信息已确认寻回，不能再次编辑和查看"));
    }
}
