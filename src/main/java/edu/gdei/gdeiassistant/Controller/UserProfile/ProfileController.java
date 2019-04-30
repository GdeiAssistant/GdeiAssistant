package edu.gdei.gdeiassistant.Controller.UserProfile;

import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Service.Privacy.PrivacyService;
import edu.gdei.gdeiassistant.Service.Profile.UserProfileService;
import edu.gdei.gdeiassistant.Tools.LocationUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private PrivacyService privacyService;

    @RequestMapping(value = {"/introduction"})
    public ModelAndView ResolveUserIntroductionPage() {
        return new ModelAndView("Profile/introduction");
    }

    /**
     * 进入公开个人资料界面
     *
     * @param username
     * @return
     */
    @RequestMapping(value = {"/profile/user/{username}"})
    public ModelAndView ResolvePublicProfilePage(@PathVariable("username") String username) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        Profile profile = userProfileService.GetUserProfile(username);
        if (profile != null) {
            Privacy privacy = privacyService.GetPrivacySetting(username);
            modelAndView.setViewName("Profile/publicProfile");
            modelAndView.addObject("AvatarURL", userProfileService.GetUserAvatar(username));
            modelAndView.addObject("KickName", profile.getKickname());
            boolean containProfile = false;
            if (privacy.isGenderOpen()) {
                if (profile.getGender() != null && profile.getGender() != 0) {
                    if (profile.getGender() == 3) {
                        modelAndView.addObject("Gender", profile.getCustomGenderName());
                    } else {
                        modelAndView.addObject("Gender", UserProfileService.getGenderMap().get(profile.getGender()));
                    }
                    containProfile = true;
                }
            }
            if (privacy.isGenderOrientationOpen()) {
                if (profile.getGenderOrientation() != null && !profile.getGenderOrientation().equals(0)) {
                    modelAndView.addObject("GenderOrientation", UserProfileService.getGenderOrientationMap().get(profile.getGenderOrientation()));
                    containProfile = true;
                }
            }
            if (privacy.isFacultyOpen()) {
                if (profile.getFaculty() != null && !profile.getFaculty().equals(0)) {
                    modelAndView.addObject("Faculty", UserProfileService.getFacultyMap().get(profile.getFaculty()));
                    containProfile = true;
                }
            }
            if (privacy.isMajorOpen()) {
                if (StringUtils.isNotBlank(profile.getMajor())) {
                    modelAndView.addObject("Major", profile.getMajor());
                    containProfile = true;
                }
            }
            if (privacy.isRegionOpen()) {
                StringBuilder location = new StringBuilder();
                Region region = LocationUtils.getRegionMap().get(profile.getRegion());
                if (region != null) {
                    location.append(region.getName());
                    State state = region.getStateMap().get(profile.getState());
                    if (state != null) {
                        if (!state.getName().equals(region.getName())) {
                            location.append(state.getName());
                        }
                        City city = state.getCityMap().get(profile.getCity());
                        if (city != null && !city.getName().equals(state.getName())) {
                            location.append(city.getName());
                        }
                    }
                    modelAndView.addObject("Location", location.toString());
                    containProfile = true;
                }
            }
            if (privacy.isIntroductionOpen()) {
                Introduction introduction = userProfileService.GetUserIntroduction(username);
                if (introduction != null && StringUtils.isNotBlank(introduction.getIntroductionContent())) {
                    modelAndView.addObject("Introduction", introduction.getIntroductionContent());
                }
            }
            modelAndView.addObject("ContainProfile", containProfile);
        } else {
            modelAndView.setViewName("Error/commonError");
            modelAndView.addObject("ErrorTitle", "错误提示");
            modelAndView.addObject("ErrorMessage", "该用户不存在或已自主注销");
        }
        return modelAndView;
    }


}
