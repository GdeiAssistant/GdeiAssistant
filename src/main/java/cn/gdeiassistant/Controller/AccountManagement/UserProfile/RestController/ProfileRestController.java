package cn.gdeiassistant.Controller.AccountManagement.UserProfile.RestController;

import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Pojo.Entity.*;
import cn.gdeiassistant.Pojo.Profile.LocationComparator;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AccountManagement.Profile.UserProfileService;
import cn.gdeiassistant.Tools.Utils.LocationUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ProfileRestController {

    @Autowired
    private UserProfileService userProfileService;

    private final int AVATAR_MAX_SIZE = 1024 * 1024 * 2;

    /**
     * 获取头像URL信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/avatar", method = RequestMethod.GET)
    public DataJsonResult<String> GetUserAvatar(HttpServletRequest request) {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        String url = userProfileService.GetUserAvatar(request.getSession().getId());
        if (StringUtils.isBlank(url)) {
            //未上传自定义头像
            jsonResult.setSuccess(true);
            jsonResult.setData("");
        } else {
            //已上传自定义头像
            jsonResult.setSuccess(true);
            jsonResult.setData(url);
        }
        return jsonResult;
    }

    /**
     * 删除用户头像图片
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/avatar/remove", method = RequestMethod.POST)
    public JsonResult DeleteUserAvatar(HttpServletRequest request) {
        userProfileService.DeleteAvatar(request.getSession().getId());
        return new JsonResult(true);
    }

    /**
     * 更新用户头像图片
     *
     * @param request
     * @param avatar
     * @param avatarHD
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/api/avatar", method = RequestMethod.POST)
    public JsonResult UpdateUserAvatar(HttpServletRequest request
            , @RequestParam("avatar") MultipartFile avatar, @RequestParam("avatar_hd") MultipartFile avatarHD) throws IOException {
        JsonResult jsonResult = new JsonResult();
        if (avatar == null || avatar.getSize() <= 0 || avatar.getSize() >= AVATAR_MAX_SIZE
                || avatarHD == null || avatarHD.getSize() <= 0 || avatarHD.getSize() >= AVATAR_MAX_SIZE) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("上传的图片文件不合法");
        } else {
            userProfileService.UpdateAvatar(request.getSession().getId(), avatar.getInputStream());
            userProfileService.UpdateHighDefinitionAvatar(request.getSession().getId(), avatarHD.getInputStream());
            jsonResult.setSuccess(true);
        }
        return jsonResult;
    }

    /**
     * 获取用户个人资料
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/profile", method = RequestMethod.GET)
    public DataJsonResult<Profile> GetUserProfile(HttpServletRequest request) throws Exception {
        DataJsonResult<Profile> result = new DataJsonResult<>();
        Profile profile = userProfileService.GetUserProfile(request.getSession().getId());
        if (profile != null) {
            if (!LocationUtils.getRegionMap().containsKey(profile.getLocationRegion())) {
                profile.setLocationRegion("未选择");
            } else {
                //获取所在地国家/地区信息
                Region locationRegion = LocationUtils.getRegionMap().get(profile.getLocationRegion());
                if (locationRegion == null) {
                    profile.setLocationRegion("未选择");
                    profile.setLocationState("");
                    profile.setLocationCity("");
                } else {
                    profile.setLocationRegion(locationRegion.getName());
                    //获取省/州信息
                    State locationState = locationRegion.getStateMap().get(profile.getLocationState());
                    if (locationState == null) {
                        profile.setLocationState("");
                        profile.setLocationCity("");
                    } else {
                        if (locationState.getName().equals(locationRegion.getName())) {
                            profile.setLocationState(locationRegion.getName());
                        } else {
                            profile.setLocationState(locationState.getName());
                        }
                        //获取市/直辖市
                        City locationCity = locationState.getCityMap().get(profile.getLocationCity());
                        if (locationCity == null) {
                            profile.setLocationCity("");
                        } else {
                            profile.setLocationCity(locationCity.getName());
                        }
                    }
                }
                //获取家乡国家/地区信息
                Region hometownRegion = LocationUtils.getRegionMap().get(profile.getHometownRegion());
                if (hometownRegion == null) {
                    profile.setHometownRegion("未选择");
                    profile.setHometownState("");
                    profile.setHometownCity("");
                } else {
                    profile.setHometownRegion(hometownRegion.getName());
                    //获取省/州信息
                    State hometownState = hometownRegion.getStateMap().get(profile.getHometownState());
                    if (hometownState == null) {
                        profile.setHometownState("");
                        profile.setHometownCity("");
                    } else {
                        if (hometownState.getName().equals(hometownRegion.getName())) {
                            profile.setHometownState(hometownRegion.getName());
                        } else {
                            profile.setHometownState(hometownState.getName());
                        }
                        //获取市/直辖市
                        City hometownCity = hometownState.getCityMap().get(profile.getHometownCity());
                        if (hometownCity == null) {
                            profile.setHometownCity("");
                        } else {
                            profile.setHometownCity(hometownCity.getName());
                        }
                    }
                }
            }
            result.setSuccess(true);
            result.setData(profile);
        }
        return result;
    }

    /**
     * 更新用户个人简介
     *
     * @param request
     * @param introduction
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/api/introduction", method = RequestMethod.POST)
    public JsonResult UpdateIntroduction(HttpServletRequest request, String introduction) throws Exception {
        if (introduction != null && introduction.length() <= 80) {
            if (introduction.isEmpty()) {
                introduction = null;
            }
            userProfileService.UpdateIntroduction(request.getSession().getId(), introduction);
            return new JsonResult(true);
        }
        return new JsonResult(false, "个人简介长度不合法");
    }

    /**
     * 获取用户个人简介
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/introduction", method = RequestMethod.GET)
    public DataJsonResult<String> GetUserIntroduction(HttpServletRequest request) throws Exception {
        Introduction introduction = userProfileService.GetUserIntroduction(request.getSession().getId());
        return new DataJsonResult<>(true, StringUtils.isBlank(introduction.getIntroductionContent()) ? "" : introduction.getIntroductionContent());
    }

    /**
     * 加载地区列表
     *
     * @return
     */
    @RequestMapping(value = "/api/locationList", method = RequestMethod.GET)
    public DataJsonResult<List<Region>> GetRegionList() {
        DataJsonResult<List<Region>> result = new DataJsonResult<>();
        List<Region> locationList = new ArrayList<>(LocationUtils.getRegionMap().values());
        locationList.sort(new LocationComparator());
        return new DataJsonResult<>(true, locationList);
    }

    /**
     * 更新用户性别
     *
     * @param request
     * @param gender
     * @return
     */
    @RequestMapping(value = "/api/profile/gender", method = RequestMethod.POST)
    public JsonResult UpdateGender(HttpServletRequest request, int gender, @Validated @Nullable @Max(16) String customGenderName) throws Exception {
        if (gender < 0 || gender >= UserProfileService.getGenderMap().size() || (gender == 3 && StringUtils.isBlank(customGenderName))) {
            return new JsonResult(false, "请求参数不合法");
        }
        userProfileService.UpdateGender(request.getSession().getId(), gender, customGenderName);
        return new JsonResult(true);
    }

    /**
     * 更新生日日期
     *
     * @param request
     * @param year
     * @param month
     * @param date
     * @return
     */
    @RequestMapping(value = "/api/profile/birthday", method = RequestMethod.POST)
    public JsonResult UpdateBirthday(HttpServletRequest request, @Validated @Min(1900) @Max(2050) Integer year
            , @Validated @Min(1) @Max(12) Integer month, @Validated @Min(1) @Max(31) Integer date) throws Exception {
        if (year == null && month == null && date == null) {
            userProfileService.ResetBirthday(request.getSession().getId());
            return new JsonResult(true);
        }
        if (year != null && month != null && date != null) {
            userProfileService.UpdateBirthday(request.getSession().getId(), year, month, date);
            return new JsonResult(true);
        }
        return new JsonResult(false, "请求参数不合法");
    }

    /**
     * 更新用户院系
     *
     * @param request
     * @param faculty
     * @return
     */
    @RequestMapping(value = "/api/profile/faculty", method = RequestMethod.POST)
    public JsonResult UpdateFaculty(HttpServletRequest request, int faculty) throws Exception {
        if (faculty >= 0 && faculty < UserProfileService.getFacultyMap().size()) {
            userProfileService.UpdateFaculty(request.getSession().getId(), faculty);
            return new JsonResult(true);
        }
        return new JsonResult(false, "请求参数异常");
    }

    /**
     * 更新用户所在地
     *
     * @param request
     * @param region
     * @param state
     * @param city
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/location", method = RequestMethod.POST)
    public JsonResult UpdateLocation(HttpServletRequest request
            , @Validated @NotBlank @RequestParam("region") String region
            , String state, String city) throws Exception {
        //判断国家/地区代码是否合法
        if (LocationUtils.getRegionMap().containsKey(region)) {
            if (LocationUtils.getRegionMap().get(region).getStateMap() == null
                    || LocationUtils.getRegionMap().get(region).getStateMap().size() == 0) {
                //省/州为空
                userProfileService.UpdateLocation(request.getSession().getId(), region, null, null);
            } else {
                Map<String, State> stateMap = LocationUtils.getRegionMap().get(region).getStateMap();
                //判断省/州代码是否合法
                if (stateMap.containsKey(state)) {
                    if (stateMap.get(state).getCityMap() == null || stateMap.get(state).getCityMap().size() == 0) {
                        //市/直辖市为空
                        userProfileService.UpdateLocation(request.getSession().getId(), region, state, null);
                        return new JsonResult(true);
                    } else {
                        Map<String, City> cityMap = stateMap.get(state).getCityMap();
                        //判断市/直辖市代码是否合法
                        if (cityMap.containsKey(city)) {
                            userProfileService.UpdateLocation(request.getSession().getId(), region, state, city);
                            return new JsonResult(true);
                        } else {
                            return new JsonResult(false, "不合法的市/直辖市代码");
                        }
                    }
                }
                return new JsonResult(false, "不合法的省/州代码");
            }
        }
        return new JsonResult(false, "不合法的国家/地区代码");
    }

    /**
     * 更新用户家乡
     *
     * @param request
     * @param region
     * @param state
     * @param city
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/hometown", method = RequestMethod.POST)
    public JsonResult UpdateHometown(HttpServletRequest request
            , @Validated @NotBlank @RequestParam("region") String region
            , String state, String city) throws Exception {
        //判断国家/地区代码是否合法
        if (LocationUtils.getRegionMap().containsKey(region)) {
            if (LocationUtils.getRegionMap().get(region).getStateMap() == null
                    || LocationUtils.getRegionMap().get(region).getStateMap().size() == 0) {
                //省/州为空
                userProfileService.UpdateHometown(request.getSession().getId(), region, null, null);
            } else {
                Map<String, State> stateMap = LocationUtils.getRegionMap().get(region).getStateMap();
                //判断省/州代码是否合法
                if (stateMap.containsKey(state)) {
                    if (stateMap.get(state).getCityMap() == null || stateMap.get(state).getCityMap().size() == 0) {
                        //市/直辖市为空
                        userProfileService.UpdateHometown(request.getSession().getId(), region, state, null);
                        return new JsonResult(true);
                    } else {
                        Map<String, City> cityMap = stateMap.get(state).getCityMap();
                        //判断市/直辖市代码是否合法
                        if (cityMap.containsKey(city)) {
                            userProfileService.UpdateHometown(request.getSession().getId(), region, state, city);
                            return new JsonResult(true);
                        } else {
                            return new JsonResult(false, "不合法的市/直辖市代码");
                        }
                    }
                }
                return new JsonResult(false, "不合法的省/州代码");
            }
        }
        return new JsonResult(false, "不合法的国家/地区代码");
    }

    /**
     * 更新用户专业
     *
     * @param request
     * @param major
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/api/profile/major", method = RequestMethod.POST)
    public JsonResult UpdateMajor(HttpServletRequest request, @Validated @NotBlank @Length(min = 1, max = 20) String major) throws Exception {
        userProfileService.UpdateMajor(request.getSession().getId(), major);
        return new JsonResult(true);
    }

    /**
     * 更新入学年份
     *
     * @param request
     * @param enrollment
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/enrollment", method = RequestMethod.POST)
    public JsonResult UpdateEnrollment(HttpServletRequest request, @Validated @Min(1955) @RequestParam(value = "year", required = false) Integer enrollment) throws Exception {
        if (enrollment != null && enrollment > LocalDate.now().getYear()) {
            return new JsonResult(false, "请求参数不合法");
        }
        if (enrollment != null) {
            userProfileService.UpdateEnrollment(request.getSession().getId(), enrollment);
        } else {
            userProfileService.ResetEnrollment(request.getSession().getId());
        }
        return new JsonResult(true);
    }

    /**
     * 更新用户昵称
     *
     * @param request
     * @param nickname
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/api/profile/nickname", method = RequestMethod.POST)
    public JsonResult UpdateNickname(HttpServletRequest request, @Validated @NotBlank @Range(min = 1, max = 32) String nickname) throws Exception {
        userProfileService.UpdateNickname(request.getSession().getId(), nickname);
        return new JsonResult(true);
    }

    /**
     * 获取昵称信息接口
     *
     * @param request
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/rest/profile/nickname", method = RequestMethod.GET)
    public DataJsonResult<String> GetUserNickname(HttpServletRequest request
            , @RequestParam("token") String token) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        Profile profile = userProfileService.GetUserProfile(sessionId);
        String nickname = profile.getNickname();
        return new DataJsonResult<>(true, nickname);
    }

    /**
     * 获取头像信息接口
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/rest/profile/avatar", method = RequestMethod.GET)
    public DataJsonResult<String> GetUserAvatar(HttpServletRequest request
            , @RequestParam("token") String token) {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        String sessionId = (String) request.getAttribute("sessionId");
        String url = userProfileService.GetUserAvatar(sessionId);
        if (StringUtils.isBlank(url)) {
            //未上传自定义头像
            jsonResult.setSuccess(true);
            jsonResult.setData("");
        } else {
            //已上传自定义头像
            jsonResult.setSuccess(true);
            jsonResult.setData(url);
        }
        return jsonResult;
    }


    /**
     * 获取用户个人资料Rest接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/rest/profile", method = RequestMethod.POST)
    @RestAuthentication
    public DataJsonResult<Profile> GetUserProfile(HttpServletRequest request
            , @RequestParam("token") String token) throws Exception {
        DataJsonResult<Profile> jsonResult = new DataJsonResult<>();
        String sessionId = (String) request.getAttribute("sessionId");
        Profile profile = userProfileService.GetUserProfile(sessionId);
        if (!LocationUtils.getRegionMap().containsKey(profile.getLocationRegion())) {
            profile.setLocationRegion(null);
        } else {
            //获取国家/地区信息
            Region region = LocationUtils.getRegionMap().get(profile.getLocationRegion());
            if (region == null) {
                profile.setLocationRegion(null);
                profile.setLocationState(null);
                profile.setLocationCity(null);
            } else {
                profile.setLocationRegion(region.getName());
                //获取省/州信息
                State state = region.getStateMap().get(profile.getLocationState());
                if (state == null) {
                    profile.setLocationState(null);
                    profile.setLocationCity(null);
                } else {
                    if (!state.getName().equals(region.getName())) {
                        profile.setLocationState(region.getName());
                    } else {
                        profile.setLocationState(state.getName());
                    }
                    //获取市/直辖市
                    City city = state.getCityMap().get(profile.getLocationCity());
                    if (city == null) {
                        profile.setLocationCity(null);
                    } else {
                        profile.setLocationCity(city.getName());
                    }
                }
            }
            jsonResult.setSuccess(true);
            jsonResult.setData(profile);
        }
        return jsonResult;
    }
}
