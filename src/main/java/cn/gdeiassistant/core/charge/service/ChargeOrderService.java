package cn.gdeiassistant.core.charge.service;

import cn.gdeiassistant.common.exception.ChargeException.ChargeIdempotencyException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.tools.Utils.PageUtils;
import cn.gdeiassistant.core.charge.mapper.ChargeOrderMapper;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderStatus;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public ChargeOrderVO queryOrder(String orderId, String username) throws DataNotExistException {
        ChargeOrderEntity order = chargeOrderMapper.findByOrderIdAndUsername(orderId, username);
        if (order == null) {
            throw new DataNotExistException("充值订单不存在或无权访问");
        }
        return toSafeVO(order);
    }

    public List<ChargeOrderVO> queryRecentOrders(String username, Integer page, Integer size, String status) {
        int safePage = page == null ? 0 : PageUtils.requireNonNegativeStart(page);
        int safeSize = size == null ? 20 : PageUtils.normalizePageSize(0, size.intValue());
        if (safePage > Integer.MAX_VALUE / safeSize) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        int start = safePage * safeSize;
        String normalizedStatus = normalizeStatus(status);
        List<ChargeOrderEntity> orders = normalizedStatus == null
                ? chargeOrderMapper.findRecentByUsername(username, start, safeSize)
                : chargeOrderMapper.findRecentByUsernameAndStatus(username, normalizedStatus, start, safeSize);
        return orders.stream().map(this::toSafeVO).collect(Collectors.toList());
    }

    public ChargeOrderVO toSafeVO(ChargeOrderEntity order) {
        if (order == null) {
            return null;
        }
        ChargeOrderVO vo = new ChargeOrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setMessage(statusMessage(order.getStatus()));
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());
        vo.setSubmittedAt(order.getSubmittedAt());
        vo.setCompletedAt(order.getCompletedAt());
        if (ChargeOrderStatus.PROCESSING.name().equals(order.getStatus())
                || ChargeOrderStatus.UNKNOWN.name().equals(order.getStatus())) {
            vo.setRetryAfter(60);
        }
        return vo;
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

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        try {
            return ChargeOrderStatus.valueOf(normalized).name();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("请求参数不合法");
        }
    }

    private String statusMessage(String status) {
        if (ChargeOrderStatus.CREATED.name().equals(status)) {
            return "充值请求已创建。";
        }
        if (ChargeOrderStatus.PROCESSING.name().equals(status)) {
            return "充值请求正在处理中，请稍后查看结果。";
        }
        if (ChargeOrderStatus.PAYMENT_SESSION_CREATED.name().equals(status)) {
            return "支付请求已生成，请完成支付并刷新余额。该状态不代表最终到账。";
        }
        if (ChargeOrderStatus.FAILED.name().equals(status)) {
            return "充值请求失败，请确认信息后重新提交。";
        }
        if (ChargeOrderStatus.UNKNOWN.name().equals(status)) {
            return "充值状态暂无法确认，请稍后查看订单状态，避免重复提交。";
        }
        if (ChargeOrderStatus.MANUAL_REVIEW.name().equals(status)) {
            return "充值状态需要进一步核实，请等待处理。";
        }
        return "充值订单状态暂无法确认，请稍后查看。";
    }
}
