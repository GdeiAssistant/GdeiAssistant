package cn.gdeiassistant.core.campuscredential.service;

import cn.gdeiassistant.common.exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.campuscredential.mapper.CampusCredentialConsentMapper;
import cn.gdeiassistant.core.campuscredential.pojo.dto.CampusCredentialConsentDTO;
import cn.gdeiassistant.core.campuscredential.pojo.entity.CampusCredentialConsentEntity;
import cn.gdeiassistant.core.campuscredential.pojo.vo.CampusCredentialStatusVO;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CampusCredentialService {

    public static final String CONSENT_TYPE_CAMPUS_CREDENTIAL = "CAMPUS_CREDENTIAL";
    public static final String SCENE_LOGIN = "LOGIN";
    public static final String SCENE_BIND = "BIND";
    public static final String SCENE_QUICK_AUTH = "QUICK_AUTH";
    public static final String SCENE_SETTINGS = "SETTINGS";
    public static final String DEFAULT_POLICY_DATE = "2026-04-25";
    public static final String DEFAULT_EFFECTIVE_DATE = "2026-05-11";

    private static final Logger logger = LoggerFactory.getLogger(CampusCredentialService.class);
    private static final Set<String> SUPPORTED_SCENES = Set.of(
            SCENE_LOGIN, SCENE_BIND, SCENE_QUICK_AUTH, SCENE_SETTINGS
    );

    @Resource(name = "campusCredentialConsentMapper")
    private CampusCredentialConsentMapper campusCredentialConsentMapper;

    @Resource(name = "privacyMapper")
    private PrivacyMapper privacyMapper;

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    public CampusCredentialStatusVO getSelfStatus(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null || StringUtils.isBlank(user.getUsername())) {
            throw new UserNotExistException("当前用户不存在，请尝试重新登录");
        }
        return getStatusByUsername(user.getUsername());
    }

    public CampusCredentialStatusVO recordConsent(String sessionId, CampusCredentialConsentDTO body) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null || StringUtils.isBlank(user.getUsername())) {
            throw new UserNotExistException("当前用户不存在，请尝试重新登录");
        }
        return recordConsentByUsername(user.getUsername(), body);
    }

    public CampusCredentialStatusVO recordConsentByUsername(String username, CampusCredentialConsentDTO body) throws Exception {
        ensurePrivacyRow(username);
        String scene = normalizeScene(body != null ? body.getScene() : null, SCENE_SETTINGS);
        java.util.Date policyDate = parseDate(body != null ? body.getPolicyDate() : null, DEFAULT_POLICY_DATE);
        java.util.Date effectiveDate = parseDate(body != null ? body.getEffectiveDate() : null, DEFAULT_EFFECTIVE_DATE);
        CampusCredentialConsentEntity activeConsent = campusCredentialConsentMapper
                .selectLatestActiveConsent(username, CONSENT_TYPE_CAMPUS_CREDENTIAL);
        if (activeConsent == null) {
            CampusCredentialConsentEntity entity = new CampusCredentialConsentEntity();
            entity.setUsername(username);
            entity.setConsentType(CONSENT_TYPE_CAMPUS_CREDENTIAL);
            entity.setPolicyDate(policyDate);
            entity.setEffectiveDate(effectiveDate);
            entity.setScene(scene);
            entity.setConsentedAt(new java.util.Date());
            campusCredentialConsentMapper.insertConsent(entity);
        }
        privacyMapper.updateQuickAuth(true, username);
        return getStatusByUsername(username);
    }

    public CampusCredentialStatusVO revokeConsent(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null || StringUtils.isBlank(user.getUsername())) {
            throw new UserNotExistException("当前用户不存在，请尝试重新登录");
        }
        String username = user.getUsername();
        revokeActiveConsent(username, "USER_REVOKED");
        privacyMapper.updateQuickAuth(false, username);
        clearSavedCredentialState(username, sessionId);
        CampusCredentialStatusVO status = getStatusByUsername(username);
        logger.info("校园凭证授权已撤回，username={}", AnonymizeUtils.maskUsername(username));
        return status;
    }

    public CampusCredentialStatusVO deleteSavedCredential(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null || StringUtils.isBlank(user.getUsername())) {
            throw new UserNotExistException("当前用户不存在，请尝试重新登录");
        }
        String username = user.getUsername();
        revokeActiveConsent(username, "CREDENTIAL_DELETED");
        privacyMapper.updateQuickAuth(false, username);
        clearSavedCredentialState(username, sessionId);
        CampusCredentialStatusVO status = getStatusByUsername(username);
        logger.info("已删除保存的校园凭证，username={}", AnonymizeUtils.maskUsername(username));
        return status;
    }

    public CampusCredentialStatusVO updateQuickAuth(String sessionId, boolean enabled) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null || StringUtils.isBlank(user.getUsername())) {
            throw new UserNotExistException("当前用户不存在，请尝试重新登录");
        }
        String username = user.getUsername();
        ensurePrivacyRow(username);
        if (enabled) {
            if (!hasActiveConsent(username)) {
                throw new IllegalArgumentException("启用快速认证前需要先完成校园账号凭证授权");
            }
            if (!hasSavedCredential(username)) {
                throw new IllegalArgumentException("当前没有可复用的校园凭证，请重新登录并授权后再启用快速认证");
            }
            privacyMapper.updateQuickAuth(true, username);
        } else {
            privacyMapper.updateQuickAuth(false, username);
        }
        return getStatusByUsername(username);
    }

    public CampusCredentialStatusVO getStatusByUsername(String username) {
        PrivacyEntity privacyEntity = privacyMapper.selectPrivacy(username);
        UserEntity userEntity = userMapper.selectUser(username);
        CampusCredentialConsentEntity latestConsent = campusCredentialConsentMapper
                .selectLatestConsent(username, CONSENT_TYPE_CAMPUS_CREDENTIAL);
        CampusCredentialConsentEntity activeConsent = campusCredentialConsentMapper
                .selectLatestActiveConsent(username, CONSENT_TYPE_CAMPUS_CREDENTIAL);
        boolean hasSavedCredential = userEntity != null && StringUtils.isNotBlank(userEntity.getPassword());
        boolean hasActiveConsent = activeConsent != null;
        boolean quickAuthAllowed = privacyEntity != null && Boolean.TRUE.equals(privacyEntity.getQuickAuthAllow());

        CampusCredentialStatusVO status = new CampusCredentialStatusVO();
        status.setHasActiveConsent(hasActiveConsent);
        status.setHasSavedCredential(hasSavedCredential);
        status.setQuickAuthEnabled(hasActiveConsent && hasSavedCredential && quickAuthAllowed);
        status.setMaskedCampusAccount(StringUtils.isNotBlank(username) ? AnonymizeUtils.maskUsername(username) : null);
        status.setPolicyDate(resolvePolicyDate(activeConsent, latestConsent));
        status.setEffectiveDate(resolveEffectiveDate(activeConsent, latestConsent));
        if (activeConsent != null) {
            status.setConsentedAt(activeConsent.getConsentedAt());
            status.setRevokedAt(null);
        } else if (latestConsent != null) {
            status.setConsentedAt(latestConsent.getConsentedAt());
            status.setRevokedAt(latestConsent.getRevokedAt());
        }
        return status;
    }

    public boolean isEffectiveQuickAuthEnabled(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        CampusCredentialStatusVO status = getStatusByUsername(username);
        return Boolean.TRUE.equals(status.getQuickAuthEnabled());
    }

    public List<User> filterUsersWithEffectiveQuickAuth(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> filtered = new ArrayList<>();
        Set<String> deduplicatedUsernames = new LinkedHashSet<>();
        for (User user : users) {
            if (user == null || StringUtils.isBlank(user.getUsername())) {
                continue;
            }
            if (!deduplicatedUsernames.add(user.getUsername())) {
                continue;
            }
            if (isEffectiveQuickAuthEnabled(user.getUsername())) {
                filtered.add(user);
            }
        }
        return filtered;
    }

    public boolean hasActiveConsent(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        return campusCredentialConsentMapper.selectLatestActiveConsent(username, CONSENT_TYPE_CAMPUS_CREDENTIAL) != null;
    }

    public boolean hasSavedCredential(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        UserEntity userEntity = userMapper.selectUser(username);
        return userEntity != null && StringUtils.isNotBlank(userEntity.getPassword());
    }

    private void clearSavedCredentialState(String username, String sessionId) {
        userMapper.clearPassword(username);
        userCertificateService.clearReusableCredentials(username);
        if (StringUtils.isNotBlank(sessionId)) {
            try {
                HttpClientUtils.clearHttpClientCookieStore(sessionId);
            } catch (Exception e) {
                logger.warn("清理校园凭证关联 CookieStore 失败，sessionId={}", sessionId, e);
            }
            userCertificateService.clearUserLoginAndSession(sessionId);
        }
    }

    private void revokeActiveConsent(String username, String revokedReason) {
        campusCredentialConsentMapper.revokeActiveConsents(
                username,
                CONSENT_TYPE_CAMPUS_CREDENTIAL,
                new java.util.Date(),
                revokedReason
        );
    }

    private void ensurePrivacyRow(String username) {
        PrivacyEntity privacyEntity = privacyMapper.selectPrivacy(username);
        if (privacyEntity == null) {
            privacyMapper.initPrivacy(username);
        }
    }

    private String normalizeScene(String scene, String defaultScene) {
        String normalized = StringUtils.isBlank(scene) ? defaultScene : scene.trim().toUpperCase();
        if (!SUPPORTED_SCENES.contains(normalized)) {
            throw new IllegalArgumentException("授权场景不合法");
        }
        return normalized;
    }

    private java.util.Date parseDate(String value, String defaultValue) {
        String raw = StringUtils.isBlank(value) ? defaultValue : value.trim();
        try {
            LocalDate localDate = LocalDate.parse(raw);
            return Date.valueOf(localDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("授权日期格式不合法");
        }
    }

    private java.util.Date resolvePolicyDate(CampusCredentialConsentEntity activeConsent,
                                             CampusCredentialConsentEntity latestConsent) {
        if (activeConsent != null && activeConsent.getPolicyDate() != null) {
            return activeConsent.getPolicyDate();
        }
        if (latestConsent != null && latestConsent.getPolicyDate() != null) {
            return latestConsent.getPolicyDate();
        }
        return parseDate(null, DEFAULT_POLICY_DATE);
    }

    private java.util.Date resolveEffectiveDate(CampusCredentialConsentEntity activeConsent,
                                                CampusCredentialConsentEntity latestConsent) {
        if (activeConsent != null && activeConsent.getEffectiveDate() != null) {
            return activeConsent.getEffectiveDate();
        }
        if (latestConsent != null && latestConsent.getEffectiveDate() != null) {
            return latestConsent.getEffectiveDate();
        }
        return parseDate(null, DEFAULT_EFFECTIVE_DATE);
    }
}
