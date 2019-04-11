package edu.gdei.gdeiassistant.ExceptionHandler;

import edu.gdei.gdeiassistant.Constant.ConstantUtils;
import edu.gdei.gdeiassistant.Controller.Authenticate.AuthenticateRestController;
import edu.gdei.gdeiassistant.Exception.AuthenticationException.*;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthenticateRestController.class)
@Order(value = 0)
public class AuthenticationRestExceptionHandler {

    @ExceptionHandler(IDCardBlurredException.class)
    public ResponseEntity HandleIDCardBlurredException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ID_CARD_BLURRED
                , false, "身份证照片模糊，请重新拍照并上传"));
    }

    @ExceptionHandler(IDCardCopyTypeException.class)
    public ResponseEntity HandleIDCardCopyTypeException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ID_CARD_COPY_TYPE
                , false, "身份证照片为复印件，请拍摄上传原件照片"));
    }

    @ExceptionHandler(IDCardEditedException.class)
    public ResponseEntity HandleIDCardEditedException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ID_CARD_EDITED
                , false, "身份证照片被编辑过，请直接使用相机拍摄并上传"));
    }

    @ExceptionHandler(IDCardOverExposureException.class)
    public ResponseEntity HandleIDCardOverExposureException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ID_CARD_OVER_EXPOSURE
                , false, "身份证照片反光或过曝，请重新拍照并上传"));
    }

    @ExceptionHandler(IDCardReversedSideException.class)
    public ResponseEntity HandleIDCardReversedSideException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ID_CARD_REVERSED_SIDE
                , false, "身份证照片正反面颠倒，请重新拍照并上传"));
    }

    @ExceptionHandler(IDCardScreenTypeException.class)
    public ResponseEntity HandleIDCardScreenTypeException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ID_CARD_SCREEN_TYPE
                , false, "身份证照片为翻拍，请拍摄上传原件照片"));
    }

    @ExceptionHandler(IDCardTemporaryTypeException.class)
    public ResponseEntity HandleIDCardTemporaryTypeException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.ID_CARD_TEMPORARY_TYPE
                , false, "暂不支持临时身份证实名认证"));
    }

    @ExceptionHandler(NonIDCardInfoException.class)
    public ResponseEntity HandleNonIDCardInfoException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.NON_ID_CARD_INFO
                , false, "上传的照片不包含身份证信息，请重新拍照并上传"));
    }

    @ExceptionHandler(OtherTypeCardException.class)
    public ResponseEntity HandleOtherTypeCardException() {
        return ResponseEntity.ok(new JsonResult(ConstantUtils.OTHER_TYPE_CARD
                , false, "目前仅支持使用中华人民共和国第二代居民身份证进行认证"));
    }


}
