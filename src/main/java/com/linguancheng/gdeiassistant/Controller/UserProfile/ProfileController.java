package com.gdeiassistant.gdeiassistant.Controller.UserProfile;

import com.gdeiassistant.gdeiassistant.Enum.Base.BoolResultEnum;
import com.gdeiassistant.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.gdeiassistant.gdeiassistant.Enum.Base.LoginResultEnum;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.*;
import com.gdeiassistant.gdeiassistant.Pojo.Profile.*;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseJsonResult;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseResult;
import com.gdeiassistant.gdeiassistant.Pojo.Result.DataJsonResult;
import com.gdeiassistant.gdeiassistant.Service.Privacy.PrivacyService;
import com.gdeiassistant.gdeiassistant.Service.Profile.RealNameService;
import com.gdeiassistant.gdeiassistant.Service.Profile.UserProfileService;
import com.gdeiassistant.gdeiassistant.Service.UserLogin.UserLoginService;
import com.gdeiassistant.gdeiassistant.ValidGroup.User.ServiceQueryValidGroup;
import com.gdeiassistant.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Controller
public class ProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private PrivacyService privacyService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private RealNameService realNameService;

    private Map<String, Region> regionMap;

    public ProfileController() throws IOException, DocumentException {
        //加载地区信息
        Resource resource = new ClassPathResource("/config/location/location.xml");
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(resource.getInputStream());
        Element root = document.getRootElement();
        List<Element> regionElements = root.elements();
        this.regionMap = new HashMap<>();
        for (Element regionElement : regionElements) {
            String regionName = regionElement.attribute("Name").getValue();
            String regionCode = regionElement.attribute("Code").getValue();
            Region region = new Region();
            region.setName(regionName);
            region.setCode(regionCode);
            Map<String, State> stateMap = new HashMap<>();
            List<Element> stateElements = regionElement.elements();
            for (Element stateElement : stateElements) {
                String stateName = stateElement.attribute("Name").getValue();
                String stateCode = stateElement.attribute("Code").getValue();
                State state = new State();
                state.setName(stateName);
                Map<String, City> cityMap = new HashMap<>();
                List<Element> cityElements = stateElement.elements();
                for (Element cityElement : cityElements) {
                    String cityName = cityElement.attribute("Name").getValue();
                    String cityCode = cityElement.attribute("Code").getValue();
                    City city = new City();
                    city.setName(cityName);
                    cityMap.put(cityCode, city);
                }
                state.setCityMap(cityMap);
                stateMap.put(stateCode, state);
            }
            region.setStateMap(stateMap);
            this.regionMap.put(regionCode, region);
        }
    }

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
    public ModelAndView ResolvePublicProfilePage(@PathVariable("username") String username) {
        ModelAndView modelAndView = new ModelAndView();
        BaseResult<Profile, DataBaseResultEnum> profileResult = userProfileService.GetUserProfile(username);
        switch (profileResult.getResultType()) {
            case SUCCESS:
                BaseResult<Privacy, DataBaseResultEnum> privacyResult = privacyService.GetPrivacySetting(username);
                switch (privacyResult.getResultType()) {
                    case SUCCESS:
                        Profile profile = profileResult.getResultData();
                        Privacy privacy = privacyResult.getResultData();
                        modelAndView.setViewName("Profile/publicProfile");
                        modelAndView.addObject("AvatarURL", userProfileService.GetUserAvatar(username));
                        modelAndView.addObject("KickName", profile.getKickname());
                        boolean containProfile = false;
                        if (privacy.isGender()) {
                            modelAndView.addObject("Gender", profile.getGender());
                            containProfile = true;
                        }
                        if (privacy.isGenderOrientation()) {
                            modelAndView.addObject("GenderOrientation", profile.getGenderOrientation());
                            containProfile = true;
                        }
                        if (privacy.isRegion()) {
                            StringBuilder location = new StringBuilder("");
                            Region region = regionMap.get(profile.getRegion());
                            if (region == null) {
                                location.append("未�?�择");
                            } else {
                                location.append(region.getName().substring(4, region.getName().length()));
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
                            }
                            modelAndView.addObject("Location", location.toString());
                            containProfile = true;
                        }
                        if (privacy.isIntroduction()) {
                            BaseResult<String, DataBaseResultEnum> introductionResult = userProfileService.GetUserIntroduction(username);
                            switch (introductionResult.getResultType()) {
                                case SUCCESS:
                                    modelAndView.addObject("Introduction", introductionResult.getResultData());
                                    break;

                                case ERROR:
                                case INCORRECT_USERNAME:
                                    modelAndView.addObject("Introduction", "加载个人�?介失�?");
                                    break;

                                case EMPTY_RESULT:
                                    break;
                            }
                        }
                        modelAndView.addObject("ContainProfile", containProfile);
                        break;

                    default:
                        modelAndView.setViewName("Error/commonError");
                        modelAndView.addObject("ErrorTitle", "错误提示");
                        modelAndView.addObject("ErrorMessage", "该用户不存在");
                        break;
                }
                break;

            case INCORRECT_USERNAME:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "错误提示");
                modelAndView.addObject("ErrorMessage", "该用户不存在");
                break;

            case ERROR:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "错误提示");
                modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再�?");
                break;
        }
        return modelAndView;
    }

    /**
     * 获取头像URL信息Rest接口
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "/rest/api/avatar/{username}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<String> GetUserAvatar(@PathVariable("username") String username) {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        if (username == null || username.trim().isEmpty()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
        } else {
            String url = userProfileService.GetUserAvatar(username);
            if (url == null || url.trim().isEmpty()) {
                //未上传自定义头像
                jsonResult.setSuccess(true);
                jsonResult.setData("");
            } else {
                //已上传自定义头像
                jsonResult.setSuccess(true);
                jsonResult.setData(url);
            }
        }
        return jsonResult;
    }

    /**
     * 获取头像URL信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/rest/avatar", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<String> GetUserAvatar(HttpServletRequest request) {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
        } else {
            String url = userProfileService.GetUserAvatar(username);
            if (url == null || url.trim().isEmpty()) {
                //未上传自定义头像
                jsonResult.setSuccess(true);
                jsonResult.setData("");
            } else {
                //已上传自定义头像
                jsonResult.setSuccess(true);
                jsonResult.setData(url);
            }
        }
        return jsonResult;
    }

    /**
     * 更新用户头像图片
     *
     * @param request
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/rest/avatar", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult UpdateUserAvatar(HttpServletRequest request
            , @RequestParam("avatar") MultipartFile file) throws IOException {
        BaseJsonResult jsonResult = new BaseJsonResult();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
        } else {
            if (file == null || file.getSize() <= 0 || file.getSize() >= 1024 * 1024 * 2) {
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("上传的图片文件不合法");
            } else {
                userProfileService.UpdateAvatar(username, file.getInputStream());
                jsonResult.setSuccess(true);
            }
        }
        return jsonResult;
    }

    /**
     * 获取用户个人资料Rest接口
     *
     * @param request
     * @param user
     * @return
     */
    @RequestMapping(value = "/rest/profile", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<Profile> GetUserProfile(HttpServletRequest request
            , @Validated(value = UserLoginValidGroup.class) User user) {
        DataJsonResult<Profile> jsonResult = new DataJsonResult<>();
        BaseResult<User, LoginResultEnum> loginResult = userLoginService.UserLogin(request, user, true);
        switch (loginResult.getResultType()) {
            case LOGIN_SUCCESS:
                BaseResult<Profile, DataBaseResultEnum> result = userProfileService.GetUserProfile(user.getUsername());
                switch (result.getResultType()) {
                    case SUCCESS:
                        Profile profile = result.getResultData();
                        if (!regionMap.containsKey(profile.getRegion())) {
                            profile.setRegion(null);
                        } else {
                            //获取国家/地区信息
                            Region region = regionMap.get(profile.getRegion());
                            if (region == null) {
                                profile.setRegion(null);
                                profile.setState(null);
                                profile.setCity(null);
                            } else {
                                profile.setRegion(region.getName().substring(4, region.getName().length()));
                                //获取�?/州信�?
                                State state = region.getStateMap().get(profile.getState());
                                if (state == null) {
                                    profile.setState(null);
                                    profile.setCity(null);
                                } else {
                                    profile.setState(state.getName());
                                    //获取�?/直辖�?
                                    City city = state.getCityMap().get(profile.getCity());
                                    if (city == null) {
                                        profile.setCity(null);
                                    } else {
                                        profile.setCity(city.getName());
                                    }
                                }
                            }
                        }
                        jsonResult.setSuccess(true);
                        jsonResult.setData(profile);
                        break;

                    case INCORRECT_USERNAME:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("请求参数异常");
                        break;

                    case ERROR:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("加载个人资料失败");
                        break;
                }
                break;

            case SERVER_ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("服务器维护中，请稍后再试");
                break;

            case TIME_OUT:
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("网络连接超时，请重试");
                break;

            case PASSWORD_ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("密码已更新，请重新登�?");
                break;
        }
        return jsonResult;
    }


    /**
     * 获取用户个人资料
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/rest/profile", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<Profile> GetUserProfile(HttpServletRequest request) {
        DataJsonResult<Profile> jsonResult = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
        } else {
            BaseResult<Profile, DataBaseResultEnum> result = userProfileService.GetUserProfile(username);
            switch (result.getResultType()) {
                case SUCCESS:
                    Profile profile = result.getResultData();
                    if (!regionMap.containsKey(profile.getRegion())) {
                        profile.setRegion("未�?�择");
                    } else {
                        //获取国家/地区信息
                        Region region = regionMap.get(profile.getRegion());
                        if (region == null) {
                            profile.setRegion("未�?�择");
                            profile.setState("");
                            profile.setCity("");
                        } else {
                            profile.setRegion(region.getName().substring(4, region.getName().length()));
                            //获取�?/州信�?
                            State state = region.getStateMap().get(profile.getState());
                            if (state == null) {
                                profile.setState("");
                                profile.setCity("");
                            } else {
                                profile.setState(state.getName());
                                //获取�?/直辖�?
                                City city = state.getCityMap().get(profile.getCity());
                                if (city == null) {
                                    profile.setCity("");
                                } else {
                                    profile.setCity(city.getName());
                                }
                            }
                        }
                    }
                    jsonResult.setSuccess(true);
                    jsonResult.setData(profile);
                    break;

                case INCORRECT_USERNAME:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("请求参数异常");
                    break;

                case ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("加载个人资料失败");
                    break;
            }
        }
        return jsonResult;
    }

    /**
     * 更新用户个人�?�?
     *
     * @param request
     * @param introduction
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/rest/introduction", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult UpdateIntroduction(HttpServletRequest request, String introduction) throws
            UnsupportedEncodingException {
        BaseJsonResult updateIntroductionJsonResult = new BaseJsonResult();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            updateIntroductionJsonResult.setSuccess(false);
            updateIntroductionJsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
        } else {
            if (introduction != null && introduction.getBytes("gb2312").length <= 80) {
                if (introduction.equals("")) {
                    introduction = null;
                }
                DataBaseResultEnum updateIntroductionResultEnum = userProfileService.UpdateIntroduction(username, introduction);
                switch (updateIntroductionResultEnum) {
                    case SUCCESS:
                        updateIntroductionJsonResult.setSuccess(true);
                        break;

                    case INCORRECT_USERNAME:
                        updateIntroductionJsonResult.setSuccess(false);
                        updateIntroductionJsonResult.setErrorMessage("请求参数不合�?");
                        break;

                    case ERROR:
                        updateIntroductionJsonResult.setSuccess(false);
                        updateIntroductionJsonResult.setErrorMessage("服务器异常，请稍候再�?");
                        break;
                }
            } else {
                updateIntroductionJsonResult.setSuccess(false);
                updateIntroductionJsonResult.setErrorMessage("请求参数不合�?");
            }
        }
        return updateIntroductionJsonResult;
    }

    /**
     * 获取用户个人�?�?
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/rest/introduction", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<String> GetUserIntroduction(HttpServletRequest request) {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
        } else {
            BaseResult<String, DataBaseResultEnum> result = userProfileService.GetUserIntroduction(username);
            switch (result.getResultType()) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    jsonResult.setData(result.getResultData());
                    break;

                case INCORRECT_USERNAME:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("请求参数不合�?");
                    break;

                case ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("加载个人�?介失�?");
                    break;

                case EMPTY_RESULT:
                    jsonResult.setSuccess(true);
                    jsonResult.setData("");
                    break;
            }
        }
        return jsonResult;
    }

    @RequestMapping(value = "/rest/regionList", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<Region>> GetRegionList() {
        DataJsonResult<List<Region>> jsonResult = new DataJsonResult<>();
        if (regionMap == null || regionMap.isEmpty()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("加载国家/地区列表失败，请稍�?�再�?");
        } else {
            List<Region> regionList = new ArrayList<>(regionMap.values());
            regionList.sort(new RegionComparator());
            jsonResult.setData(regionList);
            jsonResult.setSuccess(true);
        }
        return jsonResult;
    }

    /**
     * 更新用户性别
     *
     * @param request
     * @param gender
     * @return
     */
    @RequestMapping(value = "/rest/profile/gender", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult UpdateGender(HttpServletRequest request, int gender) {
        BaseJsonResult jsonResult = new BaseJsonResult();
        if (gender < 0 || gender > 3) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数不合�?");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            if (username == null || username.trim().isEmpty()) {
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
            } else {
                DataBaseResultEnum updateProfileResultEnum = userProfileService.UpdateGender(username, gender);
                switch (updateProfileResultEnum) {
                    case SUCCESS:
                        jsonResult.setSuccess(true);
                        break;

                    case INCORRECT_USERNAME:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
                        break;

                    case ERROR:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("服务器异常，请稍候再�?");
                        break;
                }
            }
        }
        return jsonResult;
    }

    /**
     * 更新用户性取�?
     *
     * @param request
     * @param genderOrientation
     * @return
     */
    @RequestMapping(value = "/rest/profile/genderOrientation", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult UpdateGenderOrientation(HttpServletRequest request, int genderOrientation) {
        BaseJsonResult jsonResult = new BaseJsonResult();
        if (genderOrientation > 0 && genderOrientation <= 4) {
            String username = (String) request.getSession().getAttribute("username");
            if (username == null || username.trim().isEmpty()) {
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
            } else {
                DataBaseResultEnum updateProfileResultEnum = userProfileService.UpdateGenderOrientation(username, genderOrientation);
                switch (updateProfileResultEnum) {
                    case SUCCESS:
                        jsonResult.setSuccess(true);
                        break;

                    case INCORRECT_USERNAME:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
                        break;

                    case ERROR:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("服务器异常，请稍候再�?");
                        break;
                }
            }
        } else {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数异常");
        }
        return jsonResult;
    }

    /**
     * 更新用户�?在地
     *
     * @param request
     * @param region
     * @return
     */
    @RequestMapping(value = "/rest/profile/location", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult UpdateRegion(HttpServletRequest request, String region
            , String state, String city) {
        BaseJsonResult jsonResult = new BaseJsonResult();
        if (region != null && !region.trim().isEmpty()) {
            String username = (String) request.getSession().getAttribute("username");
            if (username == null || username.trim().isEmpty()) {
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
            } else {
                DataBaseResultEnum updateProfileResultEnum = null;
                //判断国家/地区代码是否合法
                if (regionMap.containsKey(region)) {
                    if (regionMap.get(region).getStateMap() == null || regionMap.get(region).getStateMap().size() == 0) {
                        //�?/州为�?
                        updateProfileResultEnum = userProfileService.UpdateRegion(username, region, null, null);
                    } else {
                        Map<String, State> stateMap = regionMap.get(region).getStateMap();
                        //判断�?/州代码是否合�?
                        if (stateMap.containsKey(state)) {
                            if (stateMap.get(state).getCityMap() == null || stateMap.get(state).getCityMap().size() == 0) {
                                //�?/直辖市为�?
                                updateProfileResultEnum = userProfileService.UpdateRegion(username, region, state, null);
                            } else {
                                Map<String, City> cityMap = stateMap.get(state).getCityMap();
                                //判断�?/直辖市代码是否合�?
                                if (cityMap.containsKey(city)) {
                                    updateProfileResultEnum = userProfileService.UpdateRegion(username, region, state, city);
                                } else {
                                    jsonResult.setSuccess(false);
                                    jsonResult.setErrorMessage("不合法的�?/直辖市代�?");
                                }
                            }
                        } else {
                            jsonResult.setSuccess(false);
                            jsonResult.setErrorMessage("不合法的�?/州代�?");
                        }
                    }
                } else {
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("不合法的国家/地区代码");
                }
                if (updateProfileResultEnum != null) {
                    switch (updateProfileResultEnum) {
                        case SUCCESS:
                            jsonResult.setSuccess(true);
                            break;

                        case INCORRECT_USERNAME:
                            jsonResult.setSuccess(false);
                            jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
                            break;

                        case ERROR:
                            jsonResult.setSuccess(false);
                            jsonResult.setErrorMessage("服务器异常，请稍候再�?");
                            break;
                    }
                }
            }
        } else {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数异常");
        }
        return jsonResult;
    }

    /**
     * 更新用户昵称
     *
     * @param request
     * @param kickname
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/rest/profile/kickname", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult UpdateKickname(HttpServletRequest request, String kickname) throws UnsupportedEncodingException {
        BaseJsonResult jsonResult = new BaseJsonResult();
        if (kickname != null && !kickname.trim().isEmpty() && kickname.getBytes("GB2312").length <= 24) {
            String username = (String) request.getSession().getAttribute("username");
            if (username == null || username.trim().isEmpty()) {
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
            } else {
                DataBaseResultEnum updateProfileResultEnum = userProfileService.UpdateKickname(username, kickname);
                switch (updateProfileResultEnum) {
                    case SUCCESS:
                        jsonResult.setSuccess(true);
                        break;

                    case INCORRECT_USERNAME:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("用户身份凭证过期，请稍�?�再�?");
                        break;

                    case ERROR:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("服务器异常，请稍候再�?");
                        break;
                }
            }
        } else {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数异常");
        }
        return jsonResult;
    }

    /**
     * 获取用户真实姓名
     *
     * @param request
     * @param user
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/api/profile/realname", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<String> getUserRealName(HttpServletRequest request
            , @Validated(ServiceQueryValidGroup.class) User user, BindingResult bindingResult) {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        if (bindingResult.hasErrors()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数不合�?");
        } else {
            BaseResult<String, BoolResultEnum> result = realNameService
                    .GetUserRealName(request, user.getUsername(), user.getPassword());
            switch (result.getResultType()) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    jsonResult.setData(result.getResultData());
                    break;

                case ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("获取真实姓名失败，请稍后再试");
                    break;
            }
        }
        return jsonResult;
    }

}
