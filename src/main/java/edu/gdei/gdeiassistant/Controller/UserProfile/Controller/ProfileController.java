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
        String avatarHDURL = userProfileService.GetUserHighDefinitionAvatar(username);
        String avatarURL = userProfileService.GetUserAvatar(username);
        modelAndView.addObject("AvatarURL", avatarURL);
        modelAndView.addObject("AvatarHDURL", avatarHDURL);
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
            modelAndView.addObject("NickName", profile.getNickname());
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
                modelAndView.addObject("Age", ChronoUnit.YEARS.between(profile.getBirthday().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()));
            }
            if (profile.getDegree() != null) {
                modelAndView.addObject("Degree", UserProfileService.getDegreeMap().get(profile.getDegree()));
            }
            if (StringUtils.isNotBlank(profile.getColleges())) {
                modelAndView.addObject("Colleges", profile.getColleges());
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
            Region locationRegion = LocationUtils.getRegionMap().get(profile.getLocationRegion());
            if (locationRegion != null) {
                String name = StringUtils.isBlank(locationRegion.getAliasesName()) ? locationRegion.getName()
                        : locationRegion.getAliasesName();
                location.append(name);
                State locationState = locationRegion.getStateMap().get(profile.getLocationState());
                if (locationState != null) {
                    String temp = StringUtils.isBlank(locationState.getAliasesName()) ? locationState.getName()
                            : locationState.getAliasesName();
                    if (!name.equals(temp)) {
                        name = temp;
                        location.append(name);
                    }
                    City locationCity = locationState.getCityMap().get(profile.getLocationCity());
                    if (locationCity != null) {
                        temp = StringUtils.isBlank(locationCity.getAliasesName()) ? locationCity.getName()
                                : locationCity.getAliasesName();
                        if (!name.equals(temp)) {
                            name = temp;
                            location.append(name);
                        }
                    }
                }
                modelAndView.addObject("Location", LocationUtils
                        .convertCountryCodeToEmoji(locationRegion.getIso()) + location.toString());
            }
            StringBuilder hometown = new StringBuilder();
            Region hometownRegion = LocationUtils.getRegionMap().get(profile.getHometownRegion());
            if (hometownRegion != null) {
                String name = StringUtils.isBlank(hometownRegion.getAliasesName()) ? hometownRegion.getName()
                        : hometownRegion.getAliasesName();
                hometown.append(name);
                State hometownState = hometownRegion.getStateMap().get(profile.getHometownState());
                if (hometownState != null) {
                    String temp = StringUtils.isBlank(hometownState.getAliasesName()) ? hometownState.getName()
                            : hometownState.getAliasesName();
                    if (!name.equals(temp)) {
                        name = temp;
                        hometown.append(name);
                    }
                    City hometownCity = hometownState.getCityMap().get(profile.getHometownCity());
                    if (hometownCity != null) {
                        temp = StringUtils.isBlank(hometownCity.getAliasesName()) ? hometownCity.getName()
                                : hometownCity.getAliasesName();
                        if (!name.equals(temp)) {
                            name = temp;
                            hometown.append(name);
                        }
                    }
                }
                modelAndView.addObject("Hometown", LocationUtils
                        .convertCountryCodeToEmoji(hometownRegion.getIso()) + hometown.toString());
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
            modelAndView.addObject("NickName", profile.getNickname());
            if (Boolean.TRUE.equals(privacy.isGenderOpen())) {
                if (profile.getGender() != null && profile.getGender() != 0) {
                    if (profile.getGender() == 3) {
                        modelAndView.addObject("Gender", profile.getCustomGenderName());
                    } else {
                        modelAndView.addObject("Gender", UserProfileService.getGenderMap().get(profile.getGender()));
                    }
                }
            }
            if (Boolean.TRUE.equals(privacy.isFacultyOpen().equals(Boolean.TRUE))) {
                if (profile.getFaculty() != null && !profile.getFaculty().equals(0)) {
                    modelAndView.addObject("Faculty", UserProfileService.getFacultyMap().get(profile.getFaculty()));
                }
            }
            if (Boolean.TRUE.equals(privacy.isMajorOpen())) {
                if (StringUtils.isNotBlank(profile.getMajor())) {
                    modelAndView.addObject("Major", profile.getMajor());
                }
            }
            if (Boolean.TRUE.equals(privacy.isEnrollmentOpen())) {
                if (profile.getEnrollment() != null) {
                    modelAndView.addObject("Enrollment", profile.getEnrollment());
                }
            }
            if (Boolean.TRUE.equals(privacy.isAgeOpen())) {
                if (profile.getBirthday() != null) {
                    modelAndView.addObject("Age", ChronoUnit.YEARS.between(profile.getBirthday().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()));
                }
            }
            if (Boolean.TRUE.equals(privacy.isDegreeOpen())) {
                if (profile.getDegree() != null) {
                    modelAndView.addObject("Degree", UserProfileService.getDegreeMap().get(profile.getDegree()));
                }
            }
            if (Boolean.TRUE.equals(privacy.isCollegesOpen())) {
                if (profile.getColleges() != null) {
                    modelAndView.addObject("Colleges", profile.getColleges());
                }
            }
            if (Boolean.TRUE.equals(privacy.isHighSchoolOpen())) {
                if (StringUtils.isNotBlank(profile.getHighSchool())) {
                    modelAndView.addObject("HighSchool", profile.getHighSchool());
                }
            }
            if (Boolean.TRUE.equals(privacy.isJuniorHighSchoolOpen())) {
                if (StringUtils.isNotBlank(profile.getJuniorHighSchool())) {
                    modelAndView.addObject("JuniorHighSchool", profile.getJuniorHighSchool());
                }
            }
            if (Boolean.TRUE.equals(privacy.isPrimarySchoolOpen())) {
                if (StringUtils.isNotBlank(profile.getPrimarySchool())) {
                    modelAndView.addObject("PrimarySchool", profile.getPrimarySchool());
                }
            }
            if (Boolean.TRUE.equals(privacy.isProfessionOpen())) {
                if (profile.getProfession() != null) {
                    modelAndView.addObject("Profession", UserProfileService.getProfessionMap()
                            .get(profile.getProfession()));
                }
            }
            if (Boolean.TRUE.equals(privacy.isLocationOpen())) {
                StringBuilder location = new StringBuilder();
                Region region = LocationUtils.getRegionMap().get(profile.getLocationRegion());
                if (region != null) {
                    String name = StringUtils.isBlank(region.getAliasesName()) ? region.getName()
                            : region.getAliasesName();
                    location.append(name);
                    State state = region.getStateMap().get(profile.getLocationState());
                    if (state != null) {
                        String temp = StringUtils.isBlank(state.getAliasesName()) ? state.getName()
                                : state.getAliasesName();
                        if (!name.equals(temp)) {
                            name = temp;
                            location.append(name);
                        }
                        City city = state.getCityMap().get(profile.getLocationCity());
                        if (city != null) {
                            temp = StringUtils.isBlank(city.getAliasesName()) ? city.getName()
                                    : city.getAliasesName();
                            if (!name.equals(temp)) {
                                name = temp;
                                location.append(name);
                            }
                        }
                    }
                    modelAndView.addObject("Location", LocationUtils
                            .convertCountryCodeToEmoji(region.getIso()) + location.toString());
                }
            }
            if (Boolean.TRUE.equals(privacy.isHometownOpen())) {
                StringBuilder hometown = new StringBuilder();
                Region region = LocationUtils.getRegionMap().get(profile.getHometownRegion());
                if (region != null) {
                    String name = StringUtils.isBlank(region.getAliasesName()) ? region.getName()
                            : region.getAliasesName();
                    hometown.append(name);
                    State state = region.getStateMap().get(profile.getHometownState());
                    if (state != null) {
                        String temp = StringUtils.isBlank(state.getAliasesName()) ? state.getName()
                                : state.getAliasesName();
                        if (!name.equals(temp)) {
                            name = temp;
                            hometown.append(name);
                        }
                        City city = state.getCityMap().get(profile.getHometownCity());
                        if (city != null) {
                            temp = StringUtils.isBlank(city.getAliasesName()) ? city.getName()
                                    : city.getAliasesName();
                            if (!name.equals(temp)) {
                                name = temp;
                                hometown.append(name);
                            }
                        }
                    }
                    modelAndView.addObject("Hometown", LocationUtils
                            .convertCountryCodeToEmoji(region.getIso()) + hometown.toString());
                }
            }
            if (Boolean.TRUE.equals(privacy.isIntroductionOpen())) {
                Introduction introduction = userProfileService.GetUserIntroduction(username);
                if (introduction != null && StringUtils.isNotBlank(introduction.getIntroductionContent())) {
                    modelAndView.addObject("Introduction", introduction.getIntroductionContent());
                }
            }
            if (Boolean.TRUE.equals(privacy.isRobotsIndexAllow())) {
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
