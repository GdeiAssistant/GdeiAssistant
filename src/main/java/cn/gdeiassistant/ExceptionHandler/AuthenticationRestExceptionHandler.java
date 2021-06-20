package cn.gdeiassistant.ExceptionHandler;

import cn.gdeiassistant.Controller.Authenticate.RestController.AuthenticateRestController;
import cn.gdeiassistant.Exception.AuthenticationException.*;
import cn.gdeiassistant.Exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Constant.ErrorConstantUtils;
import cn.gdeiassistant.Exception.AuthenticationException.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AuthenticateRestController.class)
@Order(value = 0)
public class AuthenticationRestExceptionHandler {

    @ExceptionHandler(IDCardBlurredException.class)
    public ResponseEntity HandleIDCardBlurredException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_BLURRED
                , false, "身份证照片模糊，请重新拍照并上传"));
    }

    @ExceptionHandler(IDCardCopyTypeException.class)
    public ResponseEntity HandleIDCardCopyTypeException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_COPY_TYPE
                , false, "身份证照片为复印件，请拍摄上传原件照片"));
    }

    @ExceptionHandler(IDCardEditedException.class)
    public ResponseEntity HandleIDCardEditedException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_EDITED
                , false, "身份证照片被编辑过，请直接使用相机拍摄并上传"));
    }

    @ExceptionHandler(IDCardOverExposureException.class)
    public ResponseEntity HandleIDCardOverExposureException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_OVER_EXPOSURE
                , false, "身份证照片反光或过曝，请重新拍照并上传"));
    }

    @ExceptionHandler(IDCardReversedSideException.class)
    public ResponseEntity HandleIDCardReversedSideException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_REVERSED_SIDE
                , false, "身份证照片正反面颠倒，请重新拍照并上传"));
    }

    @ExceptionHandler(IDCardScreenTypeException.class)
    public ResponseEntity HandleIDCardScreenTypeException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_SCREEN_TYPE
                , false, "身份证照片为翻拍，请拍摄上传原件照片"));
    }

    @ExceptionHandler(IDCardTemporaryTypeException.class)
    public ResponseEntity HandleIDCardTemporaryTypeException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_TEMPORARY_TYPE
                , false, "暂不支持临时身份证实名认证"));
    }

    @ExceptionHandler(NonIDCardInfoException.class)
    public ResponseEntity HandleNonIDCardInfoException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.NON_ID_CARD_INFO
                , false, "上传的照片不包含身份证信息，请重新拍照并上传"));
    }

    @ExceptionHandler(OtherTypeCardException.class)
    public ResponseEntity HandleOtherTypeCardException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.OTHER_TYPE_CARD
                , false, "目前仅支持使用中华人民共和国第二代居民身份证进行认证"));
    }

    @ExceptionHandler(IDCardVerificationException.class)
    public ResponseEntity HandleIDCardVerifiedException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_NOT_VERIFIED
                , false, "身份证信息校验不通过，请联系客服"));
    }

    @ExceptionHandler(RecognitionException.class)
    public ResponseEntity HandleRecognitionException() {
        return ResponseEntity.ok(new JsonResult(ErrorConstantUtils.ID_CARD_RECOGNITION_FAILED
                , false,"身份证图片识别失败，请确保照片符合上传规范"));
    }
}
