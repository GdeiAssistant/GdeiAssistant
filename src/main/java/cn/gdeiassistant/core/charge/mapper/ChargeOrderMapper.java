package cn.gdeiassistant.core.charge.mapper;

import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ChargeOrderMapper {

    @Insert("insert into charge_order (order_id,username,amount,status,idempotency_key_hash,"
            + "payload_fingerprint,request_id,device_id_hash,external_order_no,external_trade_no,"
            + "school_trade_no,payment_url_hash,error_code,error_message_sanitized,unknown_reason,"
            + "created_at,updated_at,submitted_at,completed_at,last_checked_at,check_count,"
            + "manual_review_at,manual_review_note,version) values (#{orderId},#{username},#{amount},"
            + "#{status},#{idempotencyKeyHash},#{payloadFingerprint},#{requestId},#{deviceIdHash},"
            + "#{externalOrderNo},#{externalTradeNo},#{schoolTradeNo},#{paymentUrlHash},#{errorCode},"
            + "#{errorMessageSanitized},#{unknownReason},#{createdAt},#{updatedAt},#{submittedAt},"
            + "#{completedAt},#{lastCheckedAt},#{checkCount},#{manualReviewAt},#{manualReviewNote},#{version})")
    void insertChargeOrder(ChargeOrderEntity chargeOrder);

    @Update("update charge_order set status=#{status},updated_at=now(),"
            + "submitted_at=coalesce(#{submittedAt},submitted_at),"
            + "completed_at=coalesce(#{completedAt},completed_at),"
            + "payment_url_hash=coalesce(#{paymentUrlHash},payment_url_hash),"
            + "error_code=coalesce(#{errorCode},error_code),"
            + "error_message_sanitized=coalesce(#{errorMessageSanitized},error_message_sanitized),"
            + "unknown_reason=coalesce(#{unknownReason},unknown_reason),"
            + "version=version+1 where order_id=#{orderId}")
    void updateChargeOrderStatus(@Param("orderId") String orderId,
                                 @Param("status") String status,
                                 @Param("submittedAt") java.util.Date submittedAt,
                                 @Param("completedAt") java.util.Date completedAt,
                                 @Param("paymentUrlHash") String paymentUrlHash,
                                 @Param("errorCode") String errorCode,
                                 @Param("errorMessageSanitized") String errorMessageSanitized,
                                 @Param("unknownReason") String unknownReason);

    @Select("select order_id,username,amount,status,idempotency_key_hash,payload_fingerprint,request_id,"
            + "device_id_hash,external_order_no,external_trade_no,school_trade_no,payment_url_hash,"
            + "error_code,error_message_sanitized,unknown_reason,created_at,updated_at,submitted_at,"
            + "completed_at,last_checked_at,check_count,manual_review_at,manual_review_note,version "
            + "from charge_order where order_id=#{orderId}")
    @Results(id = "ChargeOrder", value = {
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "idempotencyKeyHash", column = "idempotency_key_hash"),
            @Result(property = "payloadFingerprint", column = "payload_fingerprint"),
            @Result(property = "requestId", column = "request_id"),
            @Result(property = "deviceIdHash", column = "device_id_hash"),
            @Result(property = "externalOrderNo", column = "external_order_no"),
            @Result(property = "externalTradeNo", column = "external_trade_no"),
            @Result(property = "schoolTradeNo", column = "school_trade_no"),
            @Result(property = "paymentUrlHash", column = "payment_url_hash"),
            @Result(property = "errorCode", column = "error_code"),
            @Result(property = "errorMessageSanitized", column = "error_message_sanitized"),
            @Result(property = "unknownReason", column = "unknown_reason"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "submittedAt", column = "submitted_at"),
            @Result(property = "completedAt", column = "completed_at"),
            @Result(property = "lastCheckedAt", column = "last_checked_at"),
            @Result(property = "checkCount", column = "check_count"),
            @Result(property = "manualReviewAt", column = "manual_review_at"),
            @Result(property = "manualReviewNote", column = "manual_review_note"),
            @Result(property = "version", column = "version")
    })
    ChargeOrderEntity findByOrderId(String orderId);

    @Select("select order_id,username,amount,status,idempotency_key_hash,payload_fingerprint,request_id,"
            + "device_id_hash,external_order_no,external_trade_no,school_trade_no,payment_url_hash,"
            + "error_code,error_message_sanitized,unknown_reason,created_at,updated_at,submitted_at,"
            + "completed_at,last_checked_at,check_count,manual_review_at,manual_review_note,version "
            + "from charge_order where order_id=#{orderId} and username=#{username}")
    @ResultMap("ChargeOrder")
    ChargeOrderEntity findByOrderIdAndUsername(@Param("orderId") String orderId,
                                               @Param("username") String username);

    @Select("select order_id,username,amount,status,idempotency_key_hash,payload_fingerprint,request_id,"
            + "device_id_hash,external_order_no,external_trade_no,school_trade_no,payment_url_hash,"
            + "error_code,error_message_sanitized,unknown_reason,created_at,updated_at,submitted_at,"
            + "completed_at,last_checked_at,check_count,manual_review_at,manual_review_note,version "
            + "from charge_order where username=#{username} order by created_at desc limit #{start},#{size}")
    @ResultMap("ChargeOrder")
    List<ChargeOrderEntity> findRecentByUsername(@Param("username") String username,
                                                 @Param("start") int start,
                                                 @Param("size") int size);

    @Select("select order_id,username,amount,status,idempotency_key_hash,payload_fingerprint,request_id,"
            + "device_id_hash,external_order_no,external_trade_no,school_trade_no,payment_url_hash,"
            + "error_code,error_message_sanitized,unknown_reason,created_at,updated_at,submitted_at,"
            + "completed_at,last_checked_at,check_count,manual_review_at,manual_review_note,version "
            + "from charge_order where username=#{username} and status=#{status} "
            + "order by created_at desc limit #{start},#{size}")
    @ResultMap("ChargeOrder")
    List<ChargeOrderEntity> findRecentByUsernameAndStatus(@Param("username") String username,
                                                          @Param("status") String status,
                                                          @Param("start") int start,
                                                          @Param("size") int size);

    @Select("select order_id,username,amount,status,idempotency_key_hash,payload_fingerprint,request_id,"
            + "device_id_hash,external_order_no,external_trade_no,school_trade_no,payment_url_hash,"
            + "error_code,error_message_sanitized,unknown_reason,created_at,updated_at,submitted_at,"
            + "completed_at,last_checked_at,check_count,manual_review_at,manual_review_note,version "
            + "from charge_order where username=#{username} and idempotency_key_hash=#{idempotencyKeyHash} "
            + "and payload_fingerprint=#{payloadFingerprint} order by created_at desc limit 1")
    @ResultMap("ChargeOrder")
    ChargeOrderEntity findByIdempotencyKeyHash(@Param("username") String username,
                                               @Param("idempotencyKeyHash") String idempotencyKeyHash,
                                               @Param("payloadFingerprint") String payloadFingerprint);
}
