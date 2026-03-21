package cn.gdeiassistant.core.authentication.service;

import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.Authentication.AuthenticationEnum;
import cn.gdeiassistant.common.exception.AuthenticationException.*;
import cn.gdeiassistant.common.pojo.Entity.Authentication;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.authentication.mapper.AuthenticationMapper;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.AliYunAPIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthenticationService {

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
    public Authentication QueryAuthentication(String sessionId) {
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
    public void UpdateAuthentication(String sessionId
            , Authentication authentication
            , @Nullable MultipartFile[] images) throws NullIDPhotoException, InconsistentAuthenticationException, AuthenticationRecordExistException, IDPhotoCountLimitationException, IDPhotoSizeLimitationException {
        switch (AuthenticationEnum.values()[authentication.getType()]) {
            case MAINLAND_CHINESE_RESIDENT_ID_CARD:
                //中国居民身份证，使用API进行审核
                aliYunAPIUtils.VerifyMainLandChineseResidentIDCard(authentication);
                //校验通过，记录实名认证
                User user = userCertificateService.getUserLoginCertificate(sessionId);
                Authentication data = authenticationMapper.selectAuthentication(user.getUsername());
                if (data == null) {
                    authenticationMapper.insertAuthentication(user.getUsername(), authentication.getType()
                            , authentication.getNumber(), authentication.getName());
                } else {
                    throw new AuthenticationRecordExistException("已存在实名认证记录");
                }
                break;

            default:
                throw new InconsistentAuthenticationException("该证件类型的实名认证暂未开放");
        }
    }

    /**
     * 取消实名认证
     *
     * @param sessionId
     */
    public void DeleteAuthentication(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Authentication authentication = authenticationMapper.selectAuthentication(user.getUsername());
        if (authentication != null) {
            authenticationMapper.deleteAuthentication(user.getUsername());
        }
    }

}
