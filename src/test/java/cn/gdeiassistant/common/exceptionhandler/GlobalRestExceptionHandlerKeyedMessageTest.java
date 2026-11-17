package cn.gdeiassistant.common.exceptionhandler;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.chargeexception.ChargeIdempotencyException;
import cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.common.exception.databaseexception.ConfirmedStateException;
import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.exception.databaseexception.NoAccessException;
import cn.gdeiassistant.common.exception.deliveryexception.DeliveryOrderTakenException;
import cn.gdeiassistant.common.exception.expressexception.CorrectRecordException;
import cn.gdeiassistant.common.exception.verificationexception.SendEmailException;
import cn.gdeiassistant.common.i18n.MessageCatalog;
import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalRestExceptionHandlerKeyedMessageTest {

    private final GlobalRestExceptionHandler handler = new GlobalRestExceptionHandler();

    private static MockHttpServletRequest requestWithLanguage(String language) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (language != null) {
            request.addHeader("Accept-Language", language);
        }
        return request;
    }

    @Test
    void keyedExceptionsRenderDirectlyInTargetLanguageWithArgs() {
        MockHttpServletRequest enRequest = requestWithLanguage("en-US");

        ResponseEntity<JsonResult> dataMissing = handler.handleDataNotExistException(
                DataNotExistException.ofKey("express.confessionMissing"), enRequest);
        assertEquals(200, dataMissing.getStatusCode().value());
        assertEquals(ErrorConstantUtils.DATA_NOT_EXIST, dataMissing.getBody().getCode());
        assertEquals(MessageCatalog.get("express.confessionMissing", "en"), dataMissing.getBody().getMessage());

        ResponseEntity<JsonResult> parameterizedServerError = handler.handleServerErrorException(
                ServerErrorException.ofKey("common.tools.sourceKeyTraversal", "upload/../secret"), enRequest);
        assertEquals(500, parameterizedServerError.getStatusCode().value());
        assertEquals(ErrorConstantUtils.INTERNAL_SERVER_ERROR, parameterizedServerError.getBody().getCode());
        assertEquals(
                MessageCatalog.get("common.tools.sourceKeyTraversal", "en", "upload/../secret"),
                parameterizedServerError.getBody().getMessage());

        ResponseEntity<JsonResult> featureError = handler.handleFeatureNotEnabledException(
                FeatureNotEnabledException.ofKey("auth.userMissing"), requestWithLanguage("ja"));
        assertEquals(200, featureError.getStatusCode().value());
        assertEquals(MessageCatalog.get("auth.userMissing", "ja"), featureError.getBody().getMessage());

        ResponseEntity<JsonResult> chargeError = handler.handleChargeIdempotencyException(
                ChargeIdempotencyException.ofKey(40901, "request.invalid"), enRequest);
        assertEquals(200, chargeError.getStatusCode().value());
        assertEquals(40901, chargeError.getBody().getCode());
        assertEquals(MessageCatalog.get("request.invalid", "en"), chargeError.getBody().getMessage());
    }

    @Test
    void legacySingleArgExceptionsKeepMessageCatalogFallback() {
        MockHttpServletRequest enRequest = requestWithLanguage("en");

        ResponseEntity<JsonResult> legacy = handler.handleDataNotExistException(
                new DataNotExistException(Messages.get("express.confessionMissing")), enRequest);
        assertEquals(MessageCatalog.get("express.confessionMissing", "en"), legacy.getBody().getMessage());

        ResponseEntity<JsonResult> timeoutDefault = handler.handleNetWorkTimeoutException(
                new NetWorkTimeoutException("legacy"), enRequest);
        assertEquals(ErrorConstantUtils.NETWORK_TIMEOUT, timeoutDefault.getBody().getCode());
        assertEquals(MessageCatalog.get("request.networkTimeoutRetry", "en"), timeoutDefault.getBody().getMessage());

        ResponseEntity<JsonResult> timeoutKeyed = handler.handleNetWorkTimeoutException(
                NetWorkTimeoutException.ofKey("grade.networkTimeout"), enRequest);
        assertEquals(MessageCatalog.get("grade.networkTimeout", "en"), timeoutKeyed.getBody().getMessage());
    }

    @Test
    void mergedBusinessHandlerPreservesHttp200AndLocalization() {
        MockHttpServletRequest koRequest = requestWithLanguage("ko");

        ResponseEntity<JsonResult> keyedNoAccess = handler.handleBusinessMessageException(
                NoAccessException.ofKey("request.invalid"), koRequest);
        assertEquals(200, keyedNoAccess.getStatusCode().value());
        assertFalse(keyedNoAccess.getBody().isSuccess());
        assertEquals(MessageCatalog.get("request.invalid", "ko"), keyedNoAccess.getBody().getMessage());

        ResponseEntity<JsonResult> legacyDelivery = handler.handleBusinessMessageException(
                new DeliveryOrderTakenException(Messages.get("express.confessionMissing")), koRequest);
        assertEquals(200, legacyDelivery.getStatusCode().value());
        assertEquals(MessageCatalog.get("express.confessionMissing", "ko"), legacyDelivery.getBody().getMessage());

        ResponseEntity<JsonResult> keyedState = handler.handleBusinessMessageException(
                ConfirmedStateException.ofKey("auth.userMissing"), koRequest);
        assertEquals(MessageCatalog.get("auth.userMissing", "ko"), keyedState.getBody().getMessage());

        ResponseEntity<JsonResult> keyedExpress = handler.handleBusinessMessageException(
                CorrectRecordException.ofKey("express.nameAlreadyGuessed"), koRequest);
        assertEquals(MessageCatalog.get("express.nameAlreadyGuessed", "ko"), keyedExpress.getBody().getMessage());

        ResponseEntity<JsonResult> keyedVerification = handler.handleVerificationException(
                SendEmailException.ofKey("request.invalid"), koRequest);
        assertEquals(MessageCatalog.get("request.invalid", "ko"), keyedVerification.getBody().getMessage());
    }
}
