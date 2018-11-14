package com.linguancheng.gdeiassistant.Controller.UserProfile;

import com.linguancheng.gdeiassistant.Pojo.Entity.*;
import com.linguancheng.gdeiassistant.Service.Privacy.PrivacyService;
import com.linguancheng.gdeiassistant.Service.Profile.UserProfileService;
import com.linguancheng.gdeiassistant.Tools.LocationUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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

    @RequestMapping(value = {"/profile"}, method = RequestMethod.GET)
    public ModelAndView ResolveUserProfilePage() {
        return new ModelAndView("Profile/profile");
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
            if (privacy.isGender()) {
                if (profile.getGender() != null && profile.getGender() != 0) {
                    if (profile.getGender() == 3) {
                        modelAndView.addObject("Gender", profile.getCustomGenderName());
                    } else {
                        modelAndView.addObject("Gender", UserProfileService.getGenderMap().get(profile.getGender()));
                    }
                    containProfile = true;
                }
            }
            if (privacy.isGenderOrientation()) {
                if (profile.getGenderOrientation() != null && !profile.getGenderOrientation().equals(0)) {
                    modelAndView.addObject("GenderOrientation", UserProfileService.getGenderOrientationMap().get(profile.getGenderOrientation()));
                    containProfile = true;
                }
            }
            if (privacy.isFaculty()) {
                if (profile.getFaculty() != null && !profile.getFaculty().equals(0)) {
                    modelAndView.addObject("Faculty", UserProfileService.getFacultyMap().get(profile.getFaculty()));
                    containProfile = true;
                }
            }
            if (privacy.isMajor()) {
                if (StringUtils.isNotBlank(profile.getMajor())) {
                    modelAndView.addObject("Major", profile.getMajor());
                    containProfile = true;
                }
            }
            if (privacy.isRegion()) {
                StringBuilder location = new StringBuilder("");
                Region region = LocationUtils.getRegionMap().get(profile.getRegion());
                if (region != null) {
                    location.append(region.getName().substring(4));
                    State state = region.getStateMap().get(profile.getState());
                    if (state != null) {
                        if (!state.getName().equals(region.getName().substring(4, region.getName().length()))) {
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
            if (privacy.isIntroduction()) {
                Introduction introduction = userProfileService.GetUserIntroduction(username);
                if (introduction != null && StringUtils.isNotBlank(introduction.getIntroductionContent())) {
                    modelAndView.addObject("Introduction", introduction);
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
