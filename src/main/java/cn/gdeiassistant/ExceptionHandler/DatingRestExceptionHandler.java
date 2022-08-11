package cn.gdeiassistant.ExceptionHandler;

import cn.gdeiassistant.Controller.Socialising.Delivery.RestController.DeliveryRestController;
import cn.gdeiassistant.Exception.DatingException.RepeatPickException;
import cn.gdeiassistant.Exception.DatingException.SelfPickException;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = DeliveryRestController.class)
@Order(value = 0)
public class DatingRestExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(DatingRestExceptionHandler.class);

    /**
     * 处理重复发送撩一下请求的异常
     *
     * @return
     */
    @ExceptionHandler(RepeatPickException.class)
    public ResponseEntity HandleRepeatPickException(RepeatPickException e) {
        logger.error("DatingRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "你已发送了撩一下请求，请耐心等待对方回复"));
    }

    /**
     * 处理向自己发布的卖室友信息发送撩一下请求的异常
     *
     * @return
     */
    @ExceptionHandler(SelfPickException.class)
    public ResponseEntity HandleSelfPickException(SelfPickException e) {
        logger.error("DatingRestExceptionHandler：", e);
        return ResponseEntity.ok(new JsonResult(false, "不能向自己发布的卖室友信息发送撩一下请求"));
    }
}
