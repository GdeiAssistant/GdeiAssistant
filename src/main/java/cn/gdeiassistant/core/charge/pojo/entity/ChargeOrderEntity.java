package cn.gdeiassistant.core.charge.pojo.entity;

import java.io.Serializable;
import java.util.Date;

public class ChargeOrderEntity implements Serializable {

    private String orderId;
    private String username;
    private Integer amount;
    private String status;
    private String idempotencyKeyHash;
    private String payloadFingerprint;
    private String requestId;
    private String deviceIdHash;
    private String externalOrderNo;
    private String externalTradeNo;
    private String schoolTradeNo;
    private String paymentUrlHash;
    private String errorCode;
    private String errorMessageSanitized;
    private String unknownReason;
    private Date createdAt;
    private Date updatedAt;
    private Date submittedAt;
    private Date completedAt;
    private Date lastCheckedAt;
    private Integer checkCount;
    private Date manualReviewAt;
    private String manualReviewNote;
    private Integer version;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdempotencyKeyHash() {
        return idempotencyKeyHash;
    }

    public void setIdempotencyKeyHash(String idempotencyKeyHash) {
        this.idempotencyKeyHash = idempotencyKeyHash;
    }

    public String getPayloadFingerprint() {
        return payloadFingerprint;
    }

    public void setPayloadFingerprint(String payloadFingerprint) {
        this.payloadFingerprint = payloadFingerprint;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDeviceIdHash() {
        return deviceIdHash;
    }

    public void setDeviceIdHash(String deviceIdHash) {
        this.deviceIdHash = deviceIdHash;
    }

    public String getExternalOrderNo() {
        return externalOrderNo;
    }

    public void setExternalOrderNo(String externalOrderNo) {
        this.externalOrderNo = externalOrderNo;
    }

    public String getExternalTradeNo() {
        return externalTradeNo;
    }

    public void setExternalTradeNo(String externalTradeNo) {
        this.externalTradeNo = externalTradeNo;
    }

    public String getSchoolTradeNo() {
        return schoolTradeNo;
    }

    public void setSchoolTradeNo(String schoolTradeNo) {
        this.schoolTradeNo = schoolTradeNo;
    }

    public String getPaymentUrlHash() {
        return paymentUrlHash;
    }

    public void setPaymentUrlHash(String paymentUrlHash) {
        this.paymentUrlHash = paymentUrlHash;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessageSanitized() {
        return errorMessageSanitized;
    }

    public void setErrorMessageSanitized(String errorMessageSanitized) {
        this.errorMessageSanitized = errorMessageSanitized;
    }

    public String getUnknownReason() {
        return unknownReason;
    }

    public void setUnknownReason(String unknownReason) {
        this.unknownReason = unknownReason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(Date lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public Integer getCheckCount() {
        return checkCount;
    }

    public void setCheckCount(Integer checkCount) {
        this.checkCount = checkCount;
    }

    public Date getManualReviewAt() {
        return manualReviewAt;
    }

    public void setManualReviewAt(Date manualReviewAt) {
        this.manualReviewAt = manualReviewAt;
    }

    public String getManualReviewNote() {
        return manualReviewNote;
    }

    public void setManualReviewNote(String manualReviewNote) {
        this.manualReviewNote = manualReviewNote;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
