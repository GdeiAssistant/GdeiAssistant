package com.linguancheng.gdeiassistant.ExceptionHandler;

import com.linguancheng.gdeiassistant.Controller.Ershou.ErshouRestController;
import com.linguancheng.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import com.linguancheng.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.linguancheng.gdeiassistant.Exception.DatabaseException.NoAccessException;
import com.linguancheng.gdeiassistant.Exception.DatabaseException.NotAvailableStateException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ErshouRestController.class)
public class ErshouRestExceptionHandler {

    @ExceptionHandler(DataNotExistException.class)
    public ResponseEntity ShowDataNotExistExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false, "查询的二手交易信息不存在"));
    }

    @ExceptionHandler(NoAccessException.class)
    public ResponseEntity ShowNoAccessExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false, "你没有权限编辑该二手交易信息"));
    }

    @ExceptionHandler(ConfirmedStateException.class)
    public ResponseEntity ShowUnmodifiableStateExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false, "已确认售出的商品不能再次编辑和查看"));
    }

    @ExceptionHandler(NotAvailableStateException.class)
    public ResponseEntity ShowNotAvailableStateExceptionTip() {
        return ResponseEntity.ok(new JsonResult(false, "已下架的商品暂不能查看"));
    }
}
