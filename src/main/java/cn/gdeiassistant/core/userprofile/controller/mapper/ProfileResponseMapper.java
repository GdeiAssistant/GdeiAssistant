package cn.gdeiassistant.core.userProfile.controller.mapper;

import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.common.pojo.Entity.Introduction;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.iPAddress.service.IPAddressService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userProfile.pojo.UserProfileVO;
import cn.gdeiassistant.core.userProfile.service.ProfileLocalizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
public class ProfileResponseMapper {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private IPAddressService ipAddressService;

    @Autowired
    private ProfileLocalizationService profileLocalizationService;

    /**
     * Assembles a UserProfileVO from the given ProfileVO and session context.
     */
    public UserProfileVO toUserProfileVO(ProfileVO profile, String sessionId) throws Exception {
        return toUserProfileVO(profile, sessionId, "zh-CN");
    }

    public UserProfileVO toUserProfileVO(ProfileVO profile, String sessionId, String language) throws Exception {
        if (profile.getFaculty() == null) {
            profile.setMajor(null);
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setUsername(profile.getUsername());
        vo.setNickname(profile.getNickname());
        vo.setEnrollment(profile.getEnrollment() != null ? String.valueOf(profile.getEnrollment()) : null);
        vo.setFacultyCode(profile.getFaculty());
        vo.setMajorCode(profileLocalizationService.resolveMajorCode(profile.getFaculty(), profile.getMajor()));
        // Avatar
        String avatarUrl = userProfileService.getSelfUserAvatar(sessionId);
        vo.setAvatar(StringUtils.isBlank(avatarUrl) ? "" : avatarUrl);
        // Introduction
        Introduction introduction = userProfileService.getSelfUserIntroduction(sessionId);
        vo.setIntroduction(introduction != null && StringUtils.isNotBlank(introduction.getIntroductionContent())
                ? introduction.getIntroductionContent() : "");
        // Birthday & age
        if (profile.getBirthday() != null) {
            vo.setBirthday(new SimpleDateFormat("yyyy-MM-dd").format(profile.getBirthday()));
            vo.setAge((int) ChronoUnit.YEARS.between(profile.getBirthday().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()));
        }
        // Location & hometown
        vo.setLocation(profileLocalizationService.buildLocationValue(profile, false));
        vo.setHometown(profileLocalizationService.buildLocationValue(profile, true));
        // IP area
        enrichIpArea(vo, sessionId, language);
        return vo;
    }

    private void enrichIpArea(UserProfileVO vo, String sessionId, String language) {
        try {
            IPAddressRecord ipRecord = ipAddressService.getSelfUserLatestPostTypeIPAddress(sessionId);
            String area = ipRecord != null && StringUtils.isNotBlank(ipRecord.getArea()) ? ipRecord.getArea() : "";
            vo.setIpArea(profileLocalizationService.localizeText(area, language));
        } catch (Exception ignored) {
            vo.setIpArea("");
        }
    }
}
