package cn.gdeiassistant.core.profile.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import org.springframework.stereotype.Component;
import cn.gdeiassistant.common.account.AccountDataExport;
import java.util.Map;

/**
 * The profile is reset during closure and the avatar is removed in the post-commit phase.
 */
@Component
public class ProfileAccountContributor implements AccountLifecycleContributor {

    private static final String CLOSED_NICKNAME = "已注销";

    private final ProfileMapper profileMapper;
    private final UserProfileService userProfileService;

    public ProfileAccountContributor(ProfileMapper profileMapper, UserProfileService userProfileService) {
        this.profileMapper = profileMapper;
        this.userProfileService = userProfileService;
    }

    @Override
    public void purge(AccountClosure closure) {
        profileMapper.resetUserProfile(closure.username(), CLOSED_NICKNAME);
        profileMapper.resetUserIntroduction(closure.username());
    }

    @Override
    public void cleanupAfterCommit(AccountClosure closure) {
        userProfileService.deleteAvatarForUsername(closure.username());
    }

    @Override
    public void decorateExport(AccountDataExport export) {
        Map<String, Object> profile = export.object("profile");
        if (profile == null) {
            return;
        }
        if (profile.containsKey("degree")) {
            profile.put("degree", UserProfileService.getDegreeMap().get(profile.get("degree")));
        }
        if (profile.containsKey("profession")) {
            profile.put("profession", UserProfileService.getProfessionMap().get(profile.get("profession")));
        }
        if (profile.containsKey("faculty")) {
            profile.put("faculty", UserProfileService.getFacultyMap().get(profile.get("faculty")));
        }
        if (profile.containsKey("locationRegion")) {
            AccountDataExport.resolveLocation(profile, "locationRegion", "locationState", "locationCity");
        }
        if (profile.containsKey("hometownRegion")) {
            AccountDataExport.resolveLocation(profile, "hometownRegion", "hometownState", "hometownCity");
        }
    }
}
