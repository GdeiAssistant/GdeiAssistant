package edu.gdei.gdeiassistant.Controller.UserProfile.Controller;

import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Service.Privacy.PrivacyService;
import edu.gdei.gdeiassistant.Service.Profile.UserProfileService;
import edu.gdei.gdeiassistant.Tools.LocationUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Controller
public class ProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private PrivacyService privacyService;

    @RequestMapping(value = "/introduction", method = RequestMethod.GET)
    public ModelAndView ResolveUserIntroductionPage() {
        return new ModelAndView("Profile/introduction");
    }

    @RequestMapping(value = "/avatar", method = RequestMethod.GET)
    public ModelAndView ResolveUserAvatarPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Profile/avatar");
        String username = (String) request.getSession().getAttribute("username");
        modelAndView.addObject("AvatarURL", userProfileService.GetUserHighDefinitionAvatar(username));
        return modelAndView;
    }

    @RequestMapping(value = "/profile/user", method = RequestMethod.GET)
    public ModelAndView ResolvePersonalProfilePage(HttpServletRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        Profile profile = userProfileService.GetUserProfile(username);
        if (profile != null) {
            modelAndView.setViewName("Profile/personalProfile");
            modelAndView.addObject("AvatarURL", userProfileService.GetUserAvatar(username));
            modelAndView.addObject("Username", username);
            modelAndView.addObject("KickName", profile.getKickname());
            if (profile.getGender() != null && profile.getGender() != 0) {
                if (profile.getGender() == 3) {
                    modelAndView.addObject("Gender", profile.getCustomGenderName());
                } else {
                    modelAndView.addObject("Gender", UserProfileService.getGenderMap().get(profile.getGender()));
                }
            }
            if (profile.getFaculty() != null && !profile.getFaculty().equals(0)) {
                modelAndView.addObject("Faculty", UserProfileService.getFacultyMap().get(profile.getFaculty()));
            }
            if (StringUtils.isNotBlank(profile.getMajor())) {
                modelAndView.addObject("Major", profile.getMajor());
            }
            if (profile.getEnrollment() != null) {
                modelAndView.addObject("Enrollment", profile.getEnrollment());
            }
            if (profile.getBirthday() != null) {
                modelAndView.addObject("Age", ChronoUnit.DAYS.between(profile.getBirthday().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()));
            }
            if (profile.getDegree() != null) {
                modelAndView.addObject("Degree", UserProfileService.getDegreeMap().get(profile.getDegree()));
            }
            if (StringUtils.isNotBlank(profile.getHighSchool())) {
                modelAndView.addObject("HighSchool", profile.getHighSchool());
            }
            if (StringUtils.isNotBlank(profile.getJuniorHighSchool())) {
                modelAndView.addObject("JuniorHighSchool", profile.getJuniorHighSchool());
            }
            if (StringUtils.isNotBlank(profile.getPrimarySchool())) {
                modelAndView.addObject("PrimarySchool", profile.getPrimarySchool());
            }
            if (profile.getProfession() != null) {
                modelAndView.addObject("Profession", UserProfileService.getProfessionMap()
                        .get(profile.getProfession()));
            }
            StringBuilder location = new StringBuilder();
            Region region = LocationUtils.getRegionMap().get(profile.getRegion());
            if (region != null) {
                location.append(region.getName());
                State state = region.getStateMap().get(profile.getState());
                if (state != null) {
                    if (!state.getName().equals(region.getName())) {
                        if (!state.getName().equals("香港特别行政区")
                                && !state.getName().equals("澳门特别行政区")) {
                            location.append(state.getName());
                        }
                    }
                    City city = state.getCityMap().get(profile.getCity());
                    if (city != null && !city.getName().equals(state.getName())) {
                        location.append(city.getName());
                    }
                }
                modelAndView.addObject("Location", location.toString());
            }
            Introduction introduction = userProfileService.GetUserIntroduction(username);
            if (introduction != null && StringUtils.isNotBlank(introduction.getIntroductionContent())) {
                modelAndView.addObject("Introduction", introduction.getIntroductionContent());
            }
            modelAndView.addObject("RobotsNoIndex", true);
        } else {
            modelAndView.setViewName("Error/commonError");
            modelAndView.addObject("ErrorTitle", "错误提示");
            modelAndView.addObject("ErrorMessage", "该用户不存在或已自主注销");
        }
        return modelAndView;
    }

    /**
     * 进入公开个人资料界面
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "/profile/user/{username}", method = RequestMethod.GET)
    public ModelAndView ResolvePublicProfilePage(@PathVariable("username") String username) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        Profile profile = userProfileService.GetUserProfile(username);
        if (profile != null) {
            Privacy privacy = privacyService.GetPrivacySetting(username);
            modelAndView.setViewName("Profile/publicProfile");
            modelAndView.addObject("AvatarURL", userProfileService.GetUserAvatar(username));
            modelAndView.addObject("Username", username);
            modelAndView.addObject("KickName", profile.getKickname());
            if (privacy.isGenderOpen().equals(Boolean.TRUE)) {
                if (profile.getGender() != null && profile.getGender() != 0) {
                    if (profile.getGender() == 3) {
                        modelAndView.addObject("Gender", profile.getCustomGenderName());
                    } else {
                        modelAndView.addObject("Gender", UserProfileService.getGenderMap().get(profile.getGender()));
                    }
                }
            }
            if (privacy.isFacultyOpen().equals(Boolean.TRUE)) {
                if (profile.getFaculty() != null && !profile.getFaculty().equals(0)) {
                    modelAndView.addObject("Faculty", UserProfileService.getFacultyMap().get(profile.getFaculty()));
                }
            }
            if (privacy.isMajorOpen().equals(Boolean.TRUE)) {
                if (StringUtils.isNotBlank(profile.getMajor())) {
                    modelAndView.addObject("Major", profile.getMajor());
                }
            }
            if (privacy.isEnrollmentOpen().equals(Boolean.TRUE)) {
                if (profile.getEnrollment() != null) {
                    modelAndView.addObject("Enrollment", profile.getEnrollment());
                }
            }
            if (privacy.isAgeOpen().equals(Boolean.TRUE)) {
                if (profile.getBirthday() != null) {
                    modelAndView.addObject("Age", ChronoUnit.DAYS.between(profile.getBirthday().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()));
                }
            }
            if (privacy.isDegreeOpen().equals(Boolean.TRUE)) {
                if (profile.getDegree() != null) {
                    modelAndView.addObject("Degree", UserProfileService.getDegreeMap().get(profile.getDegree()));
                }
            }
            if (privacy.isHighSchoolOpen().equals(Boolean.TRUE)) {
                if (StringUtils.isNotBlank(profile.getHighSchool())) {
                    modelAndView.addObject("HighSchool", profile.getHighSchool());
                }
            }
            if (privacy.isJuniorHighSchoolOpen().equals(Boolean.TRUE)) {
                if (StringUtils.isNotBlank(profile.getJuniorHighSchool())) {
                    modelAndView.addObject("JuniorHighSchool", profile.getJuniorHighSchool());
                }
            }
            if (privacy.isPrimarySchoolOpen().equals(Boolean.TRUE)) {
                if (StringUtils.isNotBlank(profile.getPrimarySchool())) {
                    modelAndView.addObject("PrimarySchool", profile.getPrimarySchool());
                }
            }
            if (privacy.isProfessionOpen().equals(Boolean.TRUE)) {
                if (profile.getProfession() != null) {
                    modelAndView.addObject("Profession", UserProfileService.getProfessionMap()
                            .get(profile.getProfession()));
                }
            }
            if (privacy.isRegionOpen().equals(Boolean.TRUE)) {
                StringBuilder location = new StringBuilder();
                Region region = LocationUtils.getRegionMap().get(profile.getRegion());
                if (region != null) {
                    location.append(region.getName());
                    State state = region.getStateMap().get(profile.getState());
                    if (state != null) {
                        if (!state.getName().equals(region.getName())) {
                            if (!state.getName().equals("香港特别行政区")
                                    && !state.getName().equals("澳门特别行政区")) {
                                location.append(state.getName());
                            }
                        }
                        City city = state.getCityMap().get(profile.getCity());
                        if (city != null && !city.getName().equals(state.getName())) {
                            location.append(city.getName());
                        }
                    }
                    modelAndView.addObject("Location", location.toString());
                }
            }
            if (privacy.isIntroductionOpen().equals(Boolean.TRUE)) {
                Introduction introduction = userProfileService.GetUserIntroduction(username);
                if (introduction != null && StringUtils.isNotBlank(introduction.getIntroductionContent())) {
                    modelAndView.addObject("Introduction", introduction.getIntroductionContent());
                }
            }
            if (privacy.isRobotsIndexAllow().equals(Boolean.FALSE)) {
                modelAndView.addObject("RobotsNoIndex", true);
            }
        } else {
            modelAndView.setViewName("Error/commonError");
            modelAndView.addObject("ErrorTitle", "错误提示");
            modelAndView.addObject("ErrorMessage", "该用户不存在或已自主注销");
        }
        return modelAndView;
    }
}
