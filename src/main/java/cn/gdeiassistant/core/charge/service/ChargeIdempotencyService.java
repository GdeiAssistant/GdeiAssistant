package cn.gdeiassistant.core.charge.service;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exception.ChargeException.ChargeIdempotencyException;
import cn.gdeiassistant.common.tools.SpringUtils.RedisDaoUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class ChargeIdempotencyService {

    private static final Logger logger = LoggerFactory.getLogger(ChargeIdempotencyService.class);

    public static final String ENDPOINT_CARD_CHARGE = "card-charge";

    private static final String KEY_PREFIX = "charge:idempotency";
    private static final long PROCESSING_TTL_MINUTES = 15;
    private static final long SUCCESS_TTL_MINUTES = 60;
    private static final long FAILED_TTL_MINUTES = 10;
    private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 128;

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    @Autowired
    private ObjectMapper objectMapper;

    public ChargeIdempotencyContext begin(String username, String endpoint, String rawIdempotencyKey,
                                          int amount, String deviceId) throws ChargeIdempotencyException {
        String idempotencyKey = normalizeIdempotencyKey(rawIdempotencyKey);
        String redisKey = buildRedisKey(username, endpoint, idempotencyKey);
        String fingerprint = buildPayloadFingerprint(amount, deviceId);
        long now = Instant.now().toEpochMilli();
        ChargeIdempotencyRecord processingRecord = new ChargeIdempotencyRecord(
                ChargeIdempotencyStatus.PROCESSING.name(), fingerprint, now, now);

        try {
            boolean inserted = redisDaoUtils.setIfAbsent(redisKey, serialize(processingRecord),
                    PROCESSING_TTL_MINUTES, TimeUnit.MINUTES);
            if (inserted) {
                return new ChargeIdempotencyContext(redisKey, fingerprint);
            }
            ChargeIdempotencyRecord existingRecord = readExistingRecord(redisKey);
            if (!fingerprint.equals(existingRecord.getFingerprint())) {
                throw new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT,
                        "同一幂等键不能用于不同充值参数");
            }
            throw duplicateException(existingRecord.getStatus());
        } catch (ChargeIdempotencyException e) {
            throw e;
        } catch (Exception e) {
            throw unavailableException();
        }
    }

    public String hashIdempotencyKey(String rawIdempotencyKey) throws ChargeIdempotencyException {
        return hashValue(normalizeIdempotencyKey(rawIdempotencyKey));
    }

    public String buildPayloadFingerprint(int amount, String deviceId) {
        String canonical = "amount=" + amount + "&device=" + hashValue(deviceId);
        return hashValue(canonical);
    }

    public void markProcessing(ChargeIdempotencyContext context, String orderId, String orderStatus) {
        if (context == null) {
            return;
        }
        try {
            storeRecord(context, ChargeIdempotencyStatus.PROCESSING, PROCESSING_TTL_MINUTES,
                    orderId, orderStatus);
        } catch (Exception e) {
            logger.warn("Charge idempotency processing state update failed");
        }
    }

    public void markSuccess(ChargeIdempotencyContext context) {
        markSuccess(context, null, null);
    }

    public void markSuccess(ChargeIdempotencyContext context, String orderId, String orderStatus) {
        if (context == null) {
            return;
        }
        try {
            storeRecord(context, ChargeIdempotencyStatus.SUCCESS, SUCCESS_TTL_MINUTES,
                    orderId, orderStatus);
        } catch (Exception e) {
            logger.warn("Charge idempotency success state update failed");
        }
    }

    public void markFailed(ChargeIdempotencyContext context) {
        markFailed(context, null, null);
    }

    public void markFailed(ChargeIdempotencyContext context, String orderId, String orderStatus) {
        if (context == null) {
            return;
        }
        try {
            storeRecord(context, ChargeIdempotencyStatus.FAILED, FAILED_TTL_MINUTES,
                    orderId, orderStatus);
        } catch (Exception e) {
            logger.warn("Charge idempotency failure state update failed");
        }
    }

    public void markUnknown(ChargeIdempotencyContext context, String orderId, String orderStatus) {
        if (context == null) {
            return;
        }
        try {
            storeRecord(context, ChargeIdempotencyStatus.UNKNOWN, FAILED_TTL_MINUTES,
                    orderId, orderStatus);
        } catch (Exception e) {
            logger.warn("Charge idempotency unknown state update failed");
        }
    }

    private void storeRecord(ChargeIdempotencyContext context, ChargeIdempotencyStatus status,
                             long ttlMinutes, String orderId, String orderStatus) {
        long now = Instant.now().toEpochMilli();
        ChargeIdempotencyRecord record = new ChargeIdempotencyRecord(status.name(),
                context.getFingerprint(), now, now);
        record.setOrderId(orderId);
        record.setOrderStatus(orderStatus);
        redisDaoUtils.set(context.getRedisKey(), serialize(record));
        redisDaoUtils.expire(context.getRedisKey(), ttlMinutes, TimeUnit.MINUTES);
    }

    private ChargeIdempotencyRecord readExistingRecord(String redisKey) throws ChargeIdempotencyException {
        String value = redisDaoUtils.get(redisKey);
        if (value == null || value.isBlank()) {
            throw unavailableException();
        }
        try {
            ChargeIdempotencyRecord record = objectMapper.readValue(value, ChargeIdempotencyRecord.class);
            if (record.getStatus() == null || record.getFingerprint() == null) {
                throw unavailableException();
            }
            return record;
        } catch (JsonProcessingException e) {
            throw unavailableException();
        }
    }

    private ChargeIdempotencyException duplicateException(String status) {
        ChargeIdempotencyStatus parsedStatus = ChargeIdempotencyStatus.from(status);
        if (parsedStatus == ChargeIdempotencyStatus.SUCCESS) {
            return new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT,
                    "相同充值请求已处理，请勿重复提交");
        }
        if (parsedStatus == ChargeIdempotencyStatus.FAILED) {
            return new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT,
                    "相同充值请求已失败，请使用新的幂等键重新提交");
        }
        return new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_CONFLICT,
                "相同充值请求正在处理中，请稍后查看结果");
    }

    private ChargeIdempotencyException unavailableException() {
        return new ChargeIdempotencyException(ErrorConstantUtils.CHARGE_IDEMPOTENCY_UNAVAILABLE,
                "充值幂等校验暂不可用，请稍后重试");
    }

    private String normalizeIdempotencyKey(String rawIdempotencyKey) throws ChargeIdempotencyException {
        if (rawIdempotencyKey == null || rawIdempotencyKey.trim().isEmpty()) {
            throw new ChargeIdempotencyException(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, "幂等键不合法");
        }
        String trimmed = rawIdempotencyKey.trim();
        if (trimmed.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
            throw new ChargeIdempotencyException(ErrorConstantUtils.INCORRECT_REQUEST_PARAM, "幂等键不合法");
        }
        return trimmed;
    }

    private String buildRedisKey(String username, String endpoint, String idempotencyKey) {
        return KEY_PREFIX + ":" + hashValue(username) + ":" + endpoint + ":" + hashValue(idempotencyKey);
    }

    private String serialize(ChargeIdempotencyRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Charge idempotency record serialization failed", e);
        }
    }

    private String hashValue(String value) {
        String safeValue = value == null ? "" : value;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(safeValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static class ChargeIdempotencyContext {

        private final String redisKey;
        private final String fingerprint;

        public ChargeIdempotencyContext(String redisKey, String fingerprint) {
            this.redisKey = redisKey;
            this.fingerprint = fingerprint;
        }

        public String getRedisKey() {
            return redisKey;
        }

        public String getFingerprint() {
            return fingerprint;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChargeIdempotencyRecord {

        private String status;
        private String fingerprint;
        private String orderId;
        private String orderStatus;
        private long createdAt;
        private long updatedAt;

        public ChargeIdempotencyRecord() {
        }

        public ChargeIdempotencyRecord(String status, String fingerprint, long createdAt, long updatedAt) {
            this.status = status;
            this.fingerprint = fingerprint;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }

        public long getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    private enum ChargeIdempotencyStatus {
        PROCESSING,
        SUCCESS,
        FAILED,
        UNKNOWN;

        private static ChargeIdempotencyStatus from(String value) {
            if (value == null) {
                return PROCESSING;
            }
            try {
                return ChargeIdempotencyStatus.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return PROCESSING;
            }
        }
    }
}
