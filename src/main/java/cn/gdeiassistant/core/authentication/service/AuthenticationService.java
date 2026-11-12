package cn.gdeiassistant.core.authentication.service;

import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.authentication.AuthenticationEnum;
import cn.gdeiassistant.common.exception.authenticationexception.*;
import cn.gdeiassistant.common.pojo.entity.Authentication;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.authentication.mapper.AuthenticationMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.springutils.AliYunAPIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import cn.gdeiassistant.common.i18n.Messages;

@Service
public class AuthenticationService {

    private static final Map<Integer, AuthenticationEnum> AUTHENTICATION_TYPES = Arrays.stream(AuthenticationEnum.values())
            .collect(Collectors.toUnmodifiableMap(AuthenticationEnum::getValue, Function.identity()));

    @Autowired
    private AuthenticationMapper authenticationMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private AliYunAPIUtils aliYunAPIUtils;

    /**
     * 查询实名认证状态
     *
     * @param sessionId
     * @return
     */
    public Authentication queryAuthentication(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Authentication authentication = authenticationMapper.selectAuthentication(user.getUsername());
        return authentication;
    }

    /**
     * 更新实名认证
     *
     * @param sessionId
     * @param authentication
     * @param images
     * @throws NullIDPhotoException
     * @throws InconsistentAuthenticationException
     * @throws AuthenticationRecordExistException
     * @throws IDPhotoCountLimitationException
     * @throws IDPhotoSizeLimitationException
     */
    public void updateAuthentication(String sessionId
            , Authentication authentication
            , @Nullable MultipartFile[] images) throws NullIDPhotoException, InconsistentAuthenticationException, AuthenticationRecordExistException, IDPhotoCountLimitationException, IDPhotoSizeLimitationException {
        switch (resolveAuthenticationType(authentication)) {
            case MAINLAND_CHINESE_RESIDENT_ID_CARD:
                //中国居民身份证，使用API进行审核
                aliYunAPIUtils.verifyMainLandChineseResidentIDCard(authentication);
                //校验通过，记录实名认证
                User user = userCertificateService.getUserLoginCertificate(sessionId);
                Authentication data = authenticationMapper.selectAuthentication(user.getUsername());
                if (data == null) {
                    authenticationMapper.insertAuthentication(user.getUsername(), authentication.getType()
                            , authentication.getNumber(), authentication.getName());
                } else {
                    throw new AuthenticationRecordExistException(Messages.get("authentication.recordExists"));
                }
                break;

            default:
                throw new InconsistentAuthenticationException(Messages.get("authentication.idTypeNotSupported"));
        }
    }

    private AuthenticationEnum resolveAuthenticationType(Authentication authentication) throws InconsistentAuthenticationException {
        return Optional.ofNullable(authentication)
                .map(Authentication::getType)
                .map(AUTHENTICATION_TYPES::get)
                .orElseThrow(() -> new InconsistentAuthenticationException(Messages.get("authentication.idTypeInvalid")));
    }

    /**
     * 取消实名认证
     *
     * @param sessionId
     */
    public void deleteAuthentication(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Authentication authentication = authenticationMapper.selectAuthentication(user.getUsername());
        if (authentication != null) {
            authenticationMapper.deleteAuthentication(user.getUsername());
        }
    }

}
