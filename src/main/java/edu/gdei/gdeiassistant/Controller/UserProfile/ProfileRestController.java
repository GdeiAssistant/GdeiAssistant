package edu.gdei.gdeiassistant.Controller.UserProfile;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
     * 更新用户头像图片
     *
     * @param request
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/api/avatar", method = RequestMethod.POST)
    public JsonResult UpdateUserAvatar(HttpServletRequest request
            , @RequestParam("avatar") MultipartFile file) throws IOException {
        JsonResult jsonResult = new JsonResult();
        String username = (String) request.getSession().getAttribute("username");
        if (file == null || file.getSize() <= 0 || file.getSize() >= AVATAR_MAX_SIZE) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("上传的图片文件不合法");
        } else {
            userProfileService.UpdateAvatar(username, file.getInputStream());
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
        if (!LocationUtils.getRegionMap().containsKey(profile.getRegion())) {
            profile.setRegion(null);
        } else {
            //获取国家/地区信息
            Region region = LocationUtils.getRegionMap().get(profile.getRegion());
            if (region == null) {
                profile.setRegion(null);
                profile.setState(null);
                profile.setCity(null);
            } else {
                profile.setRegion(region.getName().substring(4));
                //获取省/州信息
                State state = region.getStateMap().get(profile.getState());
                if (state == null) {
                    profile.setState(null);
                    profile.setCity(null);
                } else {
                    profile.setState(state.getName());
                    //获取市/直辖市
                    City city = state.getCityMap().get(profile.getCity());
                    if (city == null) {
                        profile.setCity(null);
                    } else {
                        profile.setCity(city.getName());
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
            if (!LocationUtils.getRegionMap().containsKey(profile.getRegion())) {
                profile.setRegion("未选择");
            } else {
                //获取国家/地区信息
                Region region = LocationUtils.getRegionMap().get(profile.getRegion());
                if (region == null) {
                    profile.setRegion("未选择");
                    profile.setState("");
                    profile.setCity("");
                } else {
                    profile.setRegion(region.getName().substring(4));
                    //获取省/州信息
                    State state = region.getStateMap().get(profile.getState());
                    if (state == null) {
                        profile.setState("");
                        profile.setCity("");
                    } else {
                        profile.setState(state.getName());
                        //获取市/直辖市
                        City city = state.getCityMap().get(profile.getCity());
                        if (city == null) {
                            profile.setCity("");
                        } else {
                            profile.setCity(city.getName());
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
     * 更新用户性取向
     *
     * @param request
     * @param genderOrientation
     * @return
     */
    @RequestMapping(value = "/api/profile/genderOrientation", method = RequestMethod.POST)
    public JsonResult UpdateGenderOrientation(HttpServletRequest request, int genderOrientation) throws Exception {
        if (genderOrientation >= 0 && genderOrientation < UserProfileService.getGenderOrientationMap().size()) {
            String username = (String) request.getSession().getAttribute("username");
            userProfileService.UpdateGenderOrientation(username, genderOrientation);
            return new JsonResult(true);
        }
        return new JsonResult(false, "请求参数异常");
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
     * @return
     */
    @RequestMapping(value = "/api/profile/location", method = RequestMethod.POST)
    public JsonResult UpdateRegion(HttpServletRequest request
            , @Validated @NotBlank @RequestParam("region") String region
            , String state, String city) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        //判断国家/地区代码是否合法
        if (LocationUtils.getRegionMap().containsKey(region)) {
            if (LocationUtils.getRegionMap().get(region).getStateMap() == null
                    || LocationUtils.getRegionMap().get(region).getStateMap().size() == 0) {
                //省/州为空
                userProfileService.UpdateRegion(username, region, null, null);
            } else {
                Map<String, State> stateMap = LocationUtils.getRegionMap().get(region).getStateMap();
                //判断省/州代码是否合法
                if (stateMap.containsKey(state)) {
                    if (stateMap.get(state).getCityMap() == null || stateMap.get(state).getCityMap().size() == 0) {
                        //市/直辖市为空
                        userProfileService.UpdateRegion(username, region, state, null);
                        return new JsonResult(true);
                    } else {
                        Map<String, City> cityMap = stateMap.get(state).getCityMap();
                        //判断市/直辖市代码是否合法
                        if (cityMap.containsKey(city)) {
                            userProfileService.UpdateRegion(username, region, state, city);
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
     * 更新用户昵称
     *
     * @param request
     * @param kickname
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/api/profile/kickname", method = RequestMethod.POST)
    public JsonResult UpdateKickname(HttpServletRequest request, @Validated @NotBlank @Range(min = 1, max = 24) String kickname) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        userProfileService.UpdateKickname(username, kickname);
        return new JsonResult(true);
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
