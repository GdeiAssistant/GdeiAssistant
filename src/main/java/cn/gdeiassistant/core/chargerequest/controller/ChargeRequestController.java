package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RequestLogPersistence;
import cn.gdeiassistant.common.annotation.RestAuthentication;
import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.ChargeException.AmountNotAvailableException;
import cn.gdeiassistant.common.exception.ChargeException.ChargeIdempotencyException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.charge.pojo.dto.ChargeRequestDTO;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderStatus;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeVO;
import cn.gdeiassistant.core.charge.service.ChargeIdempotencyService;
import cn.gdeiassistant.core.charge.service.ChargeOrderService;
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
    private ChargeOrderService chargeOrderService;

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

        String payloadFingerprint = idempotencyContext == null
                ? chargeOrderService.buildPayloadFingerprint(requestParams.getAmount(), deviceId)
                : idempotencyContext.getFingerprint();
        if (idempotencyKey != null) {
            String idempotencyKeyHash = chargeOrderService.hashIdempotencyKey(idempotencyKey);
            ChargeOrderEntity existingOrder = chargeOrderService.findByIdempotency(user.getUsername(),
                    idempotencyKeyHash, payloadFingerprint);
            if (existingOrder != null) {
                updateIdempotencyRecordFromOrder(idempotencyContext, existingOrder);
                throw duplicateOrderException(existingOrder);
            }
        }
        ChargeOrderEntity order = chargeOrderService.createOrder(user.getUsername(), requestParams.getAmount(),
                idempotencyKey, payloadFingerprint, request.getHeader("X-Request-ID"), deviceId);
        chargeOrderService.markProcessing(order.getOrderId());
        if (idempotencyContext != null) {
            chargeIdempotencyService.markProcessing(idempotencyContext, order.getOrderId(),
                    ChargeOrderStatus.PROCESSING.name());
        }

        try {
            ChargeVO charge = chargeService.ChargeRequest(sessionId, requestParams.getAmount());
            chargeService.SaveChargeLog(sessionId, requestParams.getAmount());
            chargeOrderService.markPaymentSessionCreated(order.getOrderId(), charge.getAlipayURL());
            if (idempotencyContext != null) {
                chargeIdempotencyService.markSuccess(idempotencyContext, order.getOrderId(),
                        ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
            }
            charge.setOrderId(order.getOrderId());
            charge.setStatus(ChargeOrderStatus.PAYMENT_SESSION_CREATED.name());
            charge.setMessage("支付请求已生成，请完成支付并刷新余额。");
            return new DataJsonResult<>(true, charge);
        } catch (Exception e) {
            if (shouldMarkUnknown(e)) {
                chargeOrderService.markUnknown(order.getOrderId(), e);
                if (idempotencyContext != null) {
                    chargeIdempotencyService.markUnknown(idempotencyContext, order.getOrderId(),
                            ChargeOrderStatus.UNKNOWN.name());
                }
            } else {
                chargeOrderService.markFailed(order.getOrderId(), e);
                if (idempotencyContext != null) {
                    chargeIdempotencyService.markFailed(idempotencyContext, order.getOrderId(),
                            ChargeOrderStatus.FAILED.name());
                }
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

    private boolean shouldMarkUnknown(Exception exception) {
        return !(exception instanceof AmountNotAvailableException);
    }

    private void updateIdempotencyRecordFromOrder(ChargeIdempotencyService.ChargeIdempotencyContext context,
                                                  ChargeOrderEntity order) {
        if (context == null || order == null) {
            return;
        }
        String status = order.getStatus();
        if (ChargeOrderStatus.PAYMENT_SESSION_CREATED.name().equals(status)) {
            chargeIdempotencyService.markSuccess(context, order.getOrderId(), status);
        } else if (ChargeOrderStatus.FAILED.name().equals(status)) {
            chargeIdempotencyService.markFailed(context, order.getOrderId(), status);
        } else if (ChargeOrderStatus.UNKNOWN.name().equals(status)) {
            chargeIdempotencyService.markUnknown(context, order.getOrderId(), status);
        } else {
            chargeIdempotencyService.markProcessing(context, order.getOrderId(), status);
        }
    }

    private ChargeIdempotencyException duplicateOrderException(ChargeOrderEntity order) {
        String status = order.getStatus();
        String message;
        if (ChargeOrderStatus.PAYMENT_SESSION_CREATED.name().equals(status)) {
            message = "相同充值请求已生成支付会话，请完成支付并刷新余额";
        } else if (ChargeOrderStatus.FAILED.name().equals(status)) {
            message = "相同充值请求已失败，请使用新的幂等键重新提交";
        } else if (ChargeOrderStatus.UNKNOWN.name().equals(status)) {
            message = "相同充值请求状态暂无法确认，请稍后查看结果";
        } else {
            message = "相同充值请求正在处理中，请稍后查看结果";
        }
        return new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT, message);
    }
}
