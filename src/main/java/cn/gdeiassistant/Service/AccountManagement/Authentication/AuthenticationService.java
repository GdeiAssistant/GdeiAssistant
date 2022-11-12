package cn.gdeiassistant.Service.AccountManagement.Authentication;

import cn.gdeiassistant.Enum.Authentication.AuthenticationEnum;
import cn.gdeiassistant.Exception.AuthenticationException.*;
import cn.gdeiassistant.Pojo.Entity.Authentication;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Authentication.AuthenticationMapper;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.AliYunAPIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthenticationService {

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

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
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
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
                User user = userCertificateService.GetUserLoginCertificate(sessionId);
                Authentication data = authenticationMapper.selectAuthentication(user.getUsername());
                if (data == null) {
                    authenticationMapper.insertAuthentication(user.getUsername(), authentication.getType()
                            , authentication.getNumber(), authentication.getName());
                } else {
                    throw new AuthenticationRecordExistException("已存在实名认证记录");
                }
                break;

            default:
                //其他证件类型由人工进行审核
                if (images == null) {
                    throw new NullIDPhotoException("证件照片为空");
                }
                if (images.length > 3) {
                    throw new IDPhotoCountLimitationException("证件照片数量超过限制");
                }
                for (MultipartFile file : images) {
                    if (file == null || file.isEmpty() || file.getSize() >= MAX_PICTURE_SIZE) {
                        throw new IDPhotoSizeLimitationException("证件照片文件大小超过限制");
                    }
                }

                //TODO
                //进行人工审核
                break;
        }
    }

    /**
     * 取消实名认证
     *
     * @param sessionId
     */
    public void DeleteAuthentication(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Authentication authentication = authenticationMapper.selectAuthentication(user.getUsername());
        if (authentication != null) {
            authenticationMapper.deleteAuthentication(user.getUsername());
        }
    }

}
