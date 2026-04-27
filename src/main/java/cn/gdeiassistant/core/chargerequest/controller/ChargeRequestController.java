package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RequestLogPersistence;
import cn.gdeiassistant.common.annotation.RestAuthentication;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.charge.pojo.dto.ChargeRequestDTO;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeVO;
import cn.gdeiassistant.core.charge.service.ChargeIdempotencyService;
import cn.gdeiassistant.core.charge.service.ChargeService;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ChargeRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ChargeRequestController.class);

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private ChargeIdempotencyService chargeIdempotencyService;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = "/api/card/charge", method = RequestMethod.POST)
    @RestAuthentication
    @RequestLogPersistence
    @RateLimit(maxRequests = 3, windowSeconds = 60)
    public DataJsonResult<ChargeVO> ChargeRequest(HttpServletRequest request
            , @Validated ChargeRequestDTO requestParams) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");

        // 1. 设备绑定校验：充值操作必须携带设备标识
        String deviceId = request.getHeader("X-Device-ID");
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new ServerErrorException("充值请求必须携带设备标识");
        }

        // 2. Legacy Android/iOS clients may still submit signature fields during migration.
        recordLegacySignatureFields(requestParams);

        // 3. 二次验证用户密码
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (!java.security.MessageDigest.isEqual(
                userMapper.selectUser(user.getUsername()).getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                requestParams.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new PasswordIncorrectException("充值密码验证失败");
        }

        String idempotencyKey = resolveIdempotencyKey(request);
        ChargeIdempotencyService.ChargeIdempotencyContext idempotencyContext = null;
        if (idempotencyKey != null) {
            idempotencyContext = chargeIdempotencyService.begin(user.getUsername(),
                    ChargeIdempotencyService.ENDPOINT_CARD_CHARGE, idempotencyKey,
                    requestParams.getAmount(), deviceId);
        }

        try {
            ChargeVO charge = chargeService.ChargeRequest(sessionId, requestParams.getAmount());
            chargeService.SaveChargeLog(sessionId, requestParams.getAmount());
            if (idempotencyContext != null) {
                chargeIdempotencyService.markSuccess(idempotencyContext);
            }
            return new DataJsonResult<>(true, charge);
        } catch (Exception e) {
            if (idempotencyContext != null) {
                chargeIdempotencyService.markFailed(idempotencyContext);
            }
            throw e;
        }
    }

    private void recordLegacySignatureFields(ChargeRequestDTO requestParams) {
        if (requestParams == null) {
            return;
        }
        if ((requestParams.getHmac() != null && !requestParams.getHmac().isBlank())
                || (requestParams.getTimestamp() != null && !requestParams.getTimestamp().isBlank())) {
            logger.info("Legacy charge signature fields were present and ignored");
        }
    }

    private String resolveIdempotencyKey(HttpServletRequest request) {
        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            idempotencyKey = request.getHeader("X-Idempotency-Key");
        }
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return null;
        }
        return idempotencyKey;
    }
}
