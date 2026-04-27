package cn.gdeiassistant.core.charge.service;

import cn.gdeiassistant.common.exception.ChargeException.ChargeIdempotencyException;
import cn.gdeiassistant.core.charge.mapper.ChargeOrderMapper;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ChargeOrderService {

    private static final int MAX_REQUEST_ID_LENGTH = 64;
    private static final int MAX_ERROR_CODE_LENGTH = 64;

    @Autowired
    private ChargeOrderMapper chargeOrderMapper;

    @Autowired
    private ChargeIdempotencyService chargeIdempotencyService;

    public ChargeOrderEntity createOrder(String username, int amount, String rawIdempotencyKey,
                                         String payloadFingerprint, String requestId,
                                         String deviceId) throws ChargeIdempotencyException {
        Date now = new Date();
        ChargeOrderEntity order = new ChargeOrderEntity();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUsername(username);
        order.setAmount(amount);
        order.setStatus(ChargeOrderStatus.CREATED.name());
        order.setIdempotencyKeyHash(hashIdempotencyKey(rawIdempotencyKey));
        order.setPayloadFingerprint(payloadFingerprint);
        order.setRequestId(normalizeLength(requestId, MAX_REQUEST_ID_LENGTH));
        order.setDeviceIdHash(hashNullable(deviceId));
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setCheckCount(0);
        order.setVersion(0);
        chargeOrderMapper.insertChargeOrder(order);
        return order;
    }

    public String buildPayloadFingerprint(int amount, String deviceId) {
        return chargeIdempotencyService.buildPayloadFingerprint(amount, deviceId);
    }

    public String hashIdempotencyKey(String rawIdempotencyKey) throws ChargeIdempotencyException {
        if (rawIdempotencyKey == null || rawIdempotencyKey.trim().isEmpty()) {
            return null;
        }
        return chargeIdempotencyService.hashIdempotencyKey(rawIdempotencyKey);
    }

    public void markProcessing(String orderId) {
        chargeOrderMapper.updateChargeOrderStatus(orderId, ChargeOrderStatus.PROCESSING.name(),
                new Date(), null, null, null, null, null);
    }

    public void markPaymentSessionCreated(String orderId, String paymentUrl) {
        chargeOrderMapper.updateChargeOrderStatus(orderId, ChargeOrderStatus.PAYMENT_SESSION_CREATED.name(),
                null, new Date(), hashNullable(paymentUrl), null, null, null);
    }

    public void markFailed(String orderId, Exception exception) {
        chargeOrderMapper.updateChargeOrderStatus(orderId, ChargeOrderStatus.FAILED.name(),
                null, new Date(), null, safeErrorCode(exception), "Charge request failed", null);
    }

    public void markUnknown(String orderId, Exception exception) {
        String reason = safeErrorCode(exception);
        chargeOrderMapper.updateChargeOrderStatus(orderId, ChargeOrderStatus.UNKNOWN.name(),
                null, null, null, reason, "Charge status could not be confirmed", reason);
    }

    public ChargeOrderEntity findSafeOrderStatus(String orderId, String username) {
        ChargeOrderEntity order = chargeOrderMapper.findByOrderId(orderId);
        if (order == null || !username.equals(order.getUsername())) {
            return null;
        }
        return order;
    }

    public ChargeOrderEntity findByIdempotency(String username, String idempotencyKeyHash,
                                               String payloadFingerprint) {
        if (idempotencyKeyHash == null || payloadFingerprint == null) {
            return null;
        }
        return chargeOrderMapper.findByIdempotencyKeyHash(username, idempotencyKeyHash, payloadFingerprint);
    }

    private String hashNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return hashValue(value);
    }

    private String hashValue(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String safeErrorCode(Exception exception) {
        if (exception == null) {
            return "CHARGE_ERROR";
        }
        return normalizeLength(exception.getClass().getSimpleName(), MAX_ERROR_CODE_LENGTH);
    }

    private String normalizeLength(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
