package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Exception.DeliveryException.DeliveryOrderStateUpdatedException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.DeliveryOrderTakenException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.NoAccessUpdatingException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.SelfTradingOrderException;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class DeliveryExceptionHandler {

    /**
     * 处理接单失败异常
     *
     * @return
     */
    @ExceptionHandler(DeliveryOrderTakenException.class)
    public ResponseEntity HandleAcceptOrderFailedException() {
        return ResponseEntity.ok(new JsonResult(false, "有其他用户抢先接了此单，接单失败"));
    }

    /**
     * 处理接单人和下单人相同的异常
     *
     * @return
     */
    @ExceptionHandler(SelfTradingOrderException.class)
    public ResponseEntity HandleSelfTradingException() {
        return ResponseEntity.ok(new JsonResult(false, "你不可以接受自己下的订单"));
    }

    /**
     * 处理当前用户无权修改订单信息的异常
     *
     * @return
     */
    @ExceptionHandler(NoAccessUpdatingException.class)
    public ResponseEntity HandleNoAccessUpdatingStateException() {
        return ResponseEntity.ok(new JsonResult(false, "当前用户没有权限修改该订单信息"));
    }

    /**
     * 处理订单状态已更新的异常
     *
     * @return
     */
    @ExceptionHandler(DeliveryOrderStateUpdatedException.class)
    public ResponseEntity HandleDeliveryOrderStateUpdatedException() {
        return ResponseEntity.ok(new JsonResult(false, "订单已被接单，请与接单者联系"));
    }
}
