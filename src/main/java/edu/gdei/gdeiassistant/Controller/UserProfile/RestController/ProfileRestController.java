package edu.gdei.gdeiassistant.Controller.UserProfile.RestController;

import edu.gdei.gdeiassistant.Annotation.RestAuthentication;
import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Pojo.Profile.LocationComparator;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Profile.UserProfileService;
import edu.gdei.gdeiassistant.Tools.LocationUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
        String username = (String) request.getSession().getAttribute("username");
        String url = userProfileService.GetUserAvatar(username);
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
        String username = (String) request.getSession().getAttribute("username");
        userProfileService.DeleteAvatar(username);
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
        String username = (String) request.getSession().getAttribute("username");
        if (avatar == null || avatar.getSize() <= 0 || avatar.getSize() >= AVATAR_MAX_SIZE
                || avatarHD == null || avatarHD.getSize() <= 0 || avatarHD.getSize() >= AVATAR_MAX_SIZE) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("上传的图片文件不合法");
        } else {
            userProfileService.UpdateAvatar(username, avatar.getInputStream());
            userProfileService.UpdateHighDefinitionAvatar(username, avatarHD.getInputStream());
            jsonResult.setSuccess(true);
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
        User user = (User) request.getAttribute("user");
        Profile profile = userProfileService.GetUserProfile(user.getUsername());
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

    /**
     * 获取用户个人资料
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/profile", method = RequestMethod.GET)
    public DataJsonResult<Profile> GetUserProfile(HttpServletRequest request) throws Exception {
        DataJsonResult<Profile> result = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        Profile profile = userProfileService.GetUserProfile(username);
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
        String username = (String) request.getSession().getAttribute("username");
        if (introduction != null && introduction.length() <= 80) {
            if (introduction.isEmpty()) {
                introduction = null;
            }
            userProfileService.UpdateIntroduction(username, introduction);
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
        String username = (String) request.getSession().getAttribute("username");
        Introduction introduction = userProfileService.GetUserIntroduction(username);
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
    public JsonResult UpdateGender(HttpServletRequest request, int gender, String customGenderName) throws Exception {
        if (gender < 0 || gender >= UserProfileService.getGenderMap().size() || (gender == 3 && StringUtils.isBlank(customGenderName))) {
            return new JsonResult(false, "请求参数不合法");
        }
        String username = (String) request.getSession().getAttribute("username");
        userProfileService.UpdateGender(username, gender, customGenderName);
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
        String username = (String) request.getSession().getAttribute("username");
        if (year == null && month == null && date == null) {
            userProfileService.ResetBirthday(username);
            return new JsonResult(true);
        }
        if (year != null && month != null && date != null) {
            userProfileService.UpdateBirthday(username, year, month, date);
            return new JsonResult(true);
        }
        return new JsonResult(false, "请求参数不合法");
    }

    /**
     * 更新用户学历信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/profile/degree", method = RequestMethod.POST)
    public JsonResult UpdateDegree(HttpServletRequest request, int degree) throws Exception {
        if (degree >= 0 && degree < UserProfileService.getDegreeMap().size()) {
            String username = (String) request.getSession().getAttribute("username");
            userProfileService.UpdateDegree(username, degree);
            return new JsonResult(true);
        }
        return new JsonResult(false, "请求参数异常");
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
            String username = (String) request.getSession().getAttribute("username");
            userProfileService.UpdateFaculty(username, faculty);
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
        String username = (String) request.getSession().getAttribute("username");
        //判断国家/地区代码是否合法
        if (LocationUtils.getRegionMap().containsKey(region)) {
            if (LocationUtils.getRegionMap().get(region).getStateMap() == null
                    || LocationUtils.getRegionMap().get(region).getStateMap().size() == 0) {
                //省/州为空
                userProfileService.UpdateLocation(username, region, null, null);
            } else {
                Map<String, State> stateMap = LocationUtils.getRegionMap().get(region).getStateMap();
                //判断省/州代码是否合法
                if (stateMap.containsKey(state)) {
                    if (stateMap.get(state).getCityMap() == null || stateMap.get(state).getCityMap().size() == 0) {
                        //市/直辖市为空
                        userProfileService.UpdateLocation(username, region, state, null);
                        return new JsonResult(true);
                    } else {
                        Map<String, City> cityMap = stateMap.get(state).getCityMap();
                        //判断市/直辖市代码是否合法
                        if (cityMap.containsKey(city)) {
                            userProfileService.UpdateLocation(username, region, state, city);
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
        String username = (String) request.getSession().getAttribute("username");
        //判断国家/地区代码是否合法
        if (LocationUtils.getRegionMap().containsKey(region)) {
            if (LocationUtils.getRegionMap().get(region).getStateMap() == null
                    || LocationUtils.getRegionMap().get(region).getStateMap().size() == 0) {
                //省/州为空
                userProfileService.UpdateHometown(username, region, null, null);
            } else {
                Map<String, State> stateMap = LocationUtils.getRegionMap().get(region).getStateMap();
                //判断省/州代码是否合法
                if (stateMap.containsKey(state)) {
                    if (stateMap.get(state).getCityMap() == null || stateMap.get(state).getCityMap().size() == 0) {
                        //市/直辖市为空
                        userProfileService.UpdateHometown(username, region, state, null);
                        return new JsonResult(true);
                    } else {
                        Map<String, City> cityMap = stateMap.get(state).getCityMap();
                        //判断市/直辖市代码是否合法
                        if (cityMap.containsKey(city)) {
                            userProfileService.UpdateHometown(username, region, state, city);
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
        String username = (String) request.getSession().getAttribute("username");
        userProfileService.UpdateMajor(username, major);
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
        String username = (String) request.getSession().getAttribute("username");
        if (enrollment != null) {
            userProfileService.UpdateEnrollment(username, enrollment);
        } else {
            userProfileService.ResetEnrollment(username);
        }
        return new JsonResult(true);
    }

    /**
     * 更新职业信息
     *
     * @param request
     * @param profession
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/profession", method = RequestMethod.POST)
    public JsonResult UpdateProfession(HttpServletRequest request, @Validated @NotNull @Min(0) Integer profession) throws Exception {
        if (profession == null || profession >= UserProfileService.getProfessionMap().size()) {
            return new JsonResult(false, "请求参数不合法");
        }
        String username = (String) request.getSession().getAttribute("username");
        userProfileService.UpdateProfession(username, profession);
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
    public JsonResult UpdateNickname(HttpServletRequest request, @Validated @NotBlank @Range(min = 1, max = 24) String nickname) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        userProfileService.UpdateNickname(username, nickname);
        return new JsonResult(true);
    }

    /**
     * 更新学校信息
     *
     * @param request
     * @param index
     * @param school
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/school", method = RequestMethod.POST)
    public JsonResult UpdateSchool(HttpServletRequest request, @Validated @NotNull @Min(0) @Max(2) Integer index
            , @Validated @NotBlank @Length(min = 1, max = 45) String school) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        userProfileService.UpdateSchool(username, school, index);
        return new JsonResult(true);
    }

    /**
     * 获取昵称信息Rest接口
     *
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/rest/nickname/{username}", method = RequestMethod.GET)
    public DataJsonResult<String> GetUserNickname(@PathVariable("username") String username) throws Exception {
        Profile profile = userProfileService.GetUserProfile(username);
        String nickname = profile.getNickname();
        return new DataJsonResult<>(true, nickname);
    }

    /**
     * 获取头像URL信息Rest接口
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "/rest/avatar/{username}", method = RequestMethod.GET)
    public DataJsonResult<String> GetUserAvatar(@PathVariable("username") String username) {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        String url = userProfileService.GetUserAvatar(username);
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
}
