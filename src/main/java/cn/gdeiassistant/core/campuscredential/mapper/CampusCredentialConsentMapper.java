package cn.gdeiassistant.core.campuscredential.mapper;

import cn.gdeiassistant.core.campuscredential.pojo.entity.CampusCredentialConsentEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

public interface CampusCredentialConsentMapper {

    @Select("select id,username,consent_type,policy_date,effective_date,scene,consented_at,revoked_at,revoked_reason,create_time,update_time " +
            "from campus_credential_consent where username=#{username} and consent_type=#{consentType} " +
            "order by consented_at desc,id desc limit 1")
    @Results(id = "CampusCredentialConsentEntity", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "consentType", column = "consent_type"),
            @Result(property = "policyDate", column = "policy_date", javaType = Date.class, jdbcType = JdbcType.DATE),
            @Result(property = "effectiveDate", column = "effective_date", javaType = Date.class, jdbcType = JdbcType.DATE),
            @Result(property = "scene", column = "scene"),
            @Result(property = "consentedAt", column = "consented_at"),
            @Result(property = "revokedAt", column = "revoked_at"),
            @Result(property = "revokedReason", column = "revoked_reason"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    CampusCredentialConsentEntity selectLatestConsent(@Param("username") String username,
                                                      @Param("consentType") String consentType);

    @Select("select id,username,consent_type,policy_date,effective_date,scene,consented_at,revoked_at,revoked_reason,create_time,update_time " +
            "from campus_credential_consent where username=#{username} and consent_type=#{consentType} and revoked_at is null " +
            "order by consented_at desc,id desc limit 1")
    @ResultMap("CampusCredentialConsentEntity")
    CampusCredentialConsentEntity selectLatestActiveConsent(@Param("username") String username,
                                                            @Param("consentType") String consentType);

    @Insert("insert into campus_credential_consent " +
            "(username,consent_type,policy_date,effective_date,scene,consented_at,revoked_at,revoked_reason,create_time,update_time) " +
            "values (#{username},#{consentType},#{policyDate},#{effectiveDate},#{scene},#{consentedAt},#{revokedAt},#{revokedReason},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertConsent(CampusCredentialConsentEntity entity);

    @Update("update campus_credential_consent set revoked_at=#{revokedAt},revoked_reason=#{revokedReason},update_time=now() " +
            "where username=#{username} and consent_type=#{consentType} and revoked_at is null")
    int revokeActiveConsents(@Param("username") String username,
                             @Param("consentType") String consentType,
                             @Param("revokedAt") Date revokedAt,
                             @Param("revokedReason") String revokedReason);
}
