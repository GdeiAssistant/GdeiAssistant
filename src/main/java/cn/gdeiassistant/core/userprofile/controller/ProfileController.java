package cn.gdeiassistant.core.userProfile.controller;

import cn.gdeiassistant.common.constant.OptionConstantUtils;
import cn.gdeiassistant.common.pojo.Entity.City;
import cn.gdeiassistant.common.pojo.Entity.Introduction;
import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.common.exception.CommonException.FeatureNotEnabledException;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.common.pojo.Entity.Region;
import cn.gdeiassistant.common.pojo.Entity.State;
import cn.gdeiassistant.core.profile.pojo.LocationComparator;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.userProfile.pojo.DictionaryOptionVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileOptionsVO;
import cn.gdeiassistant.core.userProfile.pojo.UserProfileVO;
import cn.gdeiassistant.core.iPAddress.service.IPAddressService;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.common.tools.Utils.LocationUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class ProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private IPAddressService ipAddressService;

    private final int AVATAR_MAX_SIZE = 1024 * 1024 * 2;

    /**
     * 获取头像URL信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/profile/avatar", method = RequestMethod.GET)
    public DataJsonResult<String> GetUserAvatar(HttpServletRequest request) throws Exception {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        String url = userProfileService.getSelfUserAvatar((String) request.getAttribute("sessionId"));
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
    @RequestMapping(value = "/api/profile/avatar", method = RequestMethod.DELETE)
    public JsonResult DeleteUserAvatar(HttpServletRequest request) throws Exception {
        userProfileService.deleteAvatar((String) request.getAttribute("sessionId"));
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
    @RequestMapping(value = "/api/profile/avatar", method = RequestMethod.POST)
    public JsonResult UpdateUserAvatar(HttpServletRequest request
            , @RequestParam(value = "avatar", required = false) MultipartFile avatar
            , @RequestParam(value = "avatar_hd", required = false) MultipartFile avatarHD
            , @RequestParam(value = "avatarKey", required = false) String avatarKey
            , @RequestParam(value = "avatarHdKey", required = false) String avatarHdKey) throws Exception {
        JsonResult jsonResult = new JsonResult();
        boolean uploadedByFile = avatar != null && avatar.getSize() > 0 && avatar.getSize() < AVATAR_MAX_SIZE
                && avatarHD != null && avatarHD.getSize() > 0 && avatarHD.getSize() < AVATAR_MAX_SIZE;
        boolean uploadedByObjectKey = StringUtils.isNotBlank(avatarKey) && StringUtils.isNotBlank(avatarHdKey);
        if (!uploadedByFile && !uploadedByObjectKey) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("上传的图片文件不合法");
        } else {
            try {
                String sessionId = (String) request.getAttribute("sessionId");
                if (uploadedByFile) {
                    userProfileService.updateAvatar(sessionId, avatar.getInputStream());
                    userProfileService.updateHighDefinitionAvatar(sessionId, avatarHD.getInputStream());
                } else {
                    userProfileService.updateAvatarByObjectKey(sessionId, avatarKey);
                    userProfileService.updateHighDefinitionAvatarByObjectKey(sessionId, avatarHdKey);
                }
                jsonResult.setSuccess(true);
            } catch (FeatureNotEnabledException e) {
                jsonResult.setCode(TrialErrorCode.AVATAR_NOT_CONFIGURED.getCode());
                jsonResult.setSuccess(false);
                jsonResult.setMessage(TrialErrorCode.AVATAR_NOT_CONFIGURED.getMessage());
            }
        }
        return jsonResult;
    }

    /**
     * 获取当前用户信息（前后端分离统一拉取接口）
     * GET /api/user/profile，返回标准格式 { "code": 200, "message": "success", "data": {...} }，需 JWT 鉴权。
     */
    @RequestMapping(value = "/api/user/profile", method = RequestMethod.GET)
    public DataJsonResult<UserProfileVO> GetCurrentUserProfile(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        ProfileVO profile = userProfileService.getSelfUserProfile(sessionId);
        if (profile == null) {
            DataJsonResult<UserProfileVO> err = new DataJsonResult<>(false, null);
            err.setCode(404);
            err.setMessage("用户不存在或未登录");
            return err;
        }
        if (profile.getFaculty() == null) {
            profile.setMajor(null);
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setUsername(profile.getUsername());
        vo.setNickname(profile.getNickname());
        vo.setMajor(profile.getMajor());
        vo.setEnrollment(profile.getEnrollment() != null ? String.valueOf(profile.getEnrollment()) : null);
        if (profile.getFaculty() != null && profile.getFaculty() != 0 && UserProfileService.getFacultyMap() != null) {
            vo.setFaculty((String) UserProfileService.getFacultyMap().get(profile.getFaculty()));
        } else {
            vo.setFaculty(null);
            vo.setMajor(null);
        }
        String avatarUrl = userProfileService.getSelfUserAvatar(sessionId);
        vo.setAvatar(StringUtils.isBlank(avatarUrl) ? "" : avatarUrl);
        Introduction introduction = userProfileService.getSelfUserIntroduction(sessionId);
        vo.setIntroduction(introduction != null && StringUtils.isNotBlank(introduction.getIntroductionContent())
                ? introduction.getIntroductionContent() : "");
        if (profile.getBirthday() != null) {
            vo.setBirthday(new SimpleDateFormat("yyyy-MM-dd").format(profile.getBirthday()));
            vo.setAge((int) ChronoUnit.YEARS.between(profile.getBirthday().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()));
        }
        vo.setLocation(buildLocationDisplayString(profile));
        vo.setHometown(buildHometownDisplayString(profile));
        try {
            IPAddressRecord ipRecord = ipAddressService.getSelfUserLatestPostTypeIPAddress(sessionId);
            vo.setIpArea(ipRecord != null && StringUtils.isNotBlank(ipRecord.getArea()) ? ipRecord.getArea() : "");
        } catch (Exception ignored) {
            vo.setIpArea("");
        }
        DataJsonResult<UserProfileVO> result = new DataJsonResult<>(true, vo);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    private String buildLocationDisplayString(ProfileVO profile) {
        if (profile == null || StringUtils.isBlank(profile.getLocationRegion())) return "";
        Region region = LocationUtils.getRegionMap().get(profile.getLocationRegion());
        if (region == null) return "";
        StringBuilder sb = new StringBuilder();
        String name = StringUtils.isBlank(region.getAliasesName()) ? region.getName() : region.getAliasesName();
        sb.append(name);
        State state = region.getStateMap() != null ? region.getStateMap().get(profile.getLocationState()) : null;
        if (state != null) {
            String temp = StringUtils.isBlank(state.getAliasesName()) ? state.getName() : state.getAliasesName();
            if (!name.equals(temp)) sb.append(temp);
            City city = state.getCityMap() != null ? state.getCityMap().get(profile.getLocationCity()) : null;
            if (city != null) {
                temp = StringUtils.isBlank(city.getAliasesName()) ? city.getName() : city.getAliasesName();
                if (!name.equals(temp)) sb.append(temp);
            }
        }
        return (region.getIso() != null ? LocationUtils.convertCountryCodeToEmoji(region.getIso()) : "") + sb.toString();
    }

    private String buildHometownDisplayString(ProfileVO profile) {
        if (profile == null || StringUtils.isBlank(profile.getHometownRegion())) return "";
        Region region = LocationUtils.getRegionMap().get(profile.getHometownRegion());
        if (region == null) return "";
        StringBuilder sb = new StringBuilder();
        String name = StringUtils.isBlank(region.getAliasesName()) ? region.getName() : region.getAliasesName();
        sb.append(name);
        State state = region.getStateMap() != null ? region.getStateMap().get(profile.getHometownState()) : null;
        if (state != null) {
            String temp = StringUtils.isBlank(state.getAliasesName()) ? state.getName() : state.getAliasesName();
            if (!name.equals(temp)) sb.append(temp);
            City city = state.getCityMap() != null ? state.getCityMap().get(profile.getHometownCity()) : null;
            if (city != null) {
                temp = StringUtils.isBlank(city.getAliasesName()) ? city.getName() : city.getAliasesName();
                if (!name.equals(temp)) sb.append(temp);
            }
        }
        return (region.getIso() != null ? LocationUtils.convertCountryCodeToEmoji(region.getIso()) : "") + sb.toString();
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
    public JsonResult UpdateIntroduction(HttpServletRequest request, @RequestBody IntroductionUpdateRequest body) throws Exception {
        String introduction = body != null ? body.getIntroduction() : null;
        if (introduction != null && introduction.length() <= 80) {
            if (introduction.isEmpty()) {
                introduction = null;
            }
            userProfileService.updateIntroduction((String) request.getAttribute("sessionId"), introduction);
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
        Introduction introduction = userProfileService.getSelfUserIntroduction((String) request.getAttribute("sessionId"));
        return new DataJsonResult<>(true, StringUtils.isBlank(introduction.getIntroductionContent()) ? "" : introduction.getIntroductionContent());
    }

    /**
     * 加载地区列表
     *
     * @return
     */
    @RequestMapping(value = "/api/profile/locations", method = RequestMethod.GET)
    public DataJsonResult<List<Region>> GetRegionList() {
        DataJsonResult<List<Region>> result = new DataJsonResult<>();
        List<Region> locationList = new ArrayList<>(LocationUtils.getRegionMap().values());
        locationList.sort(new LocationComparator());
        return new DataJsonResult<>(true, locationList);
    }

    /**
     * 获取资料与社区统一字典。
     * GET /api/profile/options
     */
    @RequestMapping(value = "/api/profile/options", method = RequestMethod.GET)
    public DataJsonResult<ProfileOptionsVO> getProfileOptions() {
        ProfileOptionsVO options = new ProfileOptionsVO();
        options.setFaculties(buildFacultyOptions());
        options.setMarketplaceItemTypes(buildDictionaryOptions(OptionConstantUtils.MARKETPLACE_ITEM_TYPE_OPTIONS));
        options.setLostFoundItemTypes(buildDictionaryOptions(OptionConstantUtils.LOST_FOUND_ITEM_TYPE_OPTIONS));
        options.setLostFoundModes(buildDictionaryOptions(OptionConstantUtils.LOST_FOUND_MODE_OPTIONS));
        return new DataJsonResult<>(true, options);
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
    public JsonResult UpdateBirthday(HttpServletRequest request, @RequestBody BirthdayUpdateRequest body) throws Exception {
        Integer year = body != null ? body.getYear() : null;
        Integer month = body != null ? body.getMonth() : null;
        Integer date = body != null ? body.getDate() : null;
        if (year == null && month == null && date == null) {
            userProfileService.resetBirthday((String) request.getAttribute("sessionId"));
            return new JsonResult(true);
        }
        if (year != null && month != null && date != null) {
            userProfileService.updateBirthday((String) request.getAttribute("sessionId"), year, month, date);
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
    public JsonResult UpdateFaculty(HttpServletRequest request, @RequestBody FacultyUpdateRequest body) throws Exception {
        Integer faculty = body != null ? body.getFaculty() : null;
        if (faculty != null && faculty >= 0 && faculty < UserProfileService.getFacultyMap().size()) {
            String sessionId = (String) request.getAttribute("sessionId");
            userProfileService.updateFaculty(sessionId, faculty);
            userProfileService.updateMajor(sessionId, null);
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
    public JsonResult UpdateLocation(HttpServletRequest request, @RequestBody LocationUpdateRequest body) throws Exception {
        if (body == null || StringUtils.isBlank(body.getRegion())) {
            return new JsonResult(false, "不合法的国家/地区代码");
        }
        String region = body.getRegion();
        String state = body.getState();
        String city = body.getCity();
        //判断国家/地区代码是否合法
        if (LocationUtils.getRegionMap().containsKey(region)) {
            if (LocationUtils.getRegionMap().get(region).getStateMap() == null
                    || LocationUtils.getRegionMap().get(region).getStateMap().size() == 0) {
                //省/州为空
                userProfileService.updateLocation((String) request.getAttribute("sessionId"), region, null, null);
                return new JsonResult(true);
            } else {
                Map<String, State> stateMap = LocationUtils.getRegionMap().get(region).getStateMap();
                //判断省/州代码是否合法
                if (state != null && stateMap.containsKey(state)) {
                    if (stateMap.get(state).getCityMap() == null || stateMap.get(state).getCityMap().size() == 0) {
                        //市/直辖市为空
                        userProfileService.updateLocation((String) request.getAttribute("sessionId"), region, state, null);
                        return new JsonResult(true);
                    } else {
                        Map<String, City> cityMap = stateMap.get(state).getCityMap();
                        //判断市/直辖市代码是否合法
                        if (city != null && cityMap.containsKey(city)) {
                            userProfileService.updateLocation((String) request.getAttribute("sessionId"), region, state, city);
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
    public JsonResult UpdateHometown(HttpServletRequest request, @RequestBody HometownUpdateRequest body) throws Exception {
        if (body == null || StringUtils.isBlank(body.getRegion())) {
            return new JsonResult(false, "不合法的国家/地区代码");
        }
        String region = body.getRegion();
        String state = body.getState();
        String city = body.getCity();
        //判断国家/地区代码是否合法
        if (LocationUtils.getRegionMap().containsKey(region)) {
            if (LocationUtils.getRegionMap().get(region).getStateMap() == null
                    || LocationUtils.getRegionMap().get(region).getStateMap().size() == 0) {
                //省/州为空
                userProfileService.updateHometown((String) request.getAttribute("sessionId"), region, null, null);
                return new JsonResult(true);
            } else {
                Map<String, State> stateMap = LocationUtils.getRegionMap().get(region).getStateMap();
                //判断省/州代码是否合法
                if (state != null && stateMap.containsKey(state)) {
                    if (stateMap.get(state).getCityMap() == null || stateMap.get(state).getCityMap().size() == 0) {
                        //市/直辖市为空
                        userProfileService.updateHometown((String) request.getAttribute("sessionId"), region, state, null);
                        return new JsonResult(true);
                    } else {
                        Map<String, City> cityMap = stateMap.get(state).getCityMap();
                        //判断市/直辖市代码是否合法
                        if (city != null && cityMap.containsKey(city)) {
                            userProfileService.updateHometown((String) request.getAttribute("sessionId"), region, state, city);
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
    public JsonResult UpdateMajor(HttpServletRequest request, @RequestBody MajorUpdateRequest body) throws Exception {
        String major = body != null ? body.getMajor() : null;
        if (major == null || major.isEmpty() || major.length() > 20) {
            return new JsonResult(false, "专业长度不合法");
        }
        userProfileService.updateMajor((String) request.getAttribute("sessionId"), major);
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
    public JsonResult UpdateEnrollment(HttpServletRequest request, @RequestBody EnrollmentUpdateRequest body) throws Exception {
        Integer enrollment = body != null ? body.getYear() : null;
        if (enrollment != null && enrollment > LocalDate.now().getYear()) {
            return new JsonResult(false, "请求参数不合法");
        }
        if (enrollment != null) {
            userProfileService.updateEnrollment((String) request.getAttribute("sessionId"), enrollment);
        } else {
            userProfileService.resetEnrollment((String) request.getAttribute("sessionId"));
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
    public JsonResult UpdateNickname(HttpServletRequest request, @RequestBody NicknameUpdateRequest body) throws Exception {
        String nickname = body != null ? body.getNickname() : null;
        if (nickname == null || nickname.isEmpty() || nickname.length() > 32) {
            return new JsonResult(false, "昵称长度不合法");
        }
        userProfileService.updateNickname((String) request.getAttribute("sessionId"), nickname);
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
    @RequestMapping(value = "/api/profile/nickname", method = RequestMethod.GET)
    public DataJsonResult<String> GetUserNickname(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        ProfileVO profile = userProfileService.getSelfUserProfile(sessionId);
        String nickname = profile.getNickname();
        return new DataJsonResult<>(true, nickname);
    }

    // ==================== 修改资料 POST 接口的 JSON 请求体 DTO ====================

    public static class IntroductionUpdateRequest {
        private String introduction;
        public String getIntroduction() { return introduction; }
        public void setIntroduction(String introduction) { this.introduction = introduction; }
    }

    public static class BirthdayUpdateRequest {
        private Integer year;
        private Integer month;
        private Integer date;
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }
        public Integer getDate() { return date; }
        public void setDate(Integer date) { this.date = date; }
    }

    public static class FacultyUpdateRequest {
        private Integer faculty;
        public Integer getFaculty() { return faculty; }
        public void setFaculty(Integer faculty) { this.faculty = faculty; }
    }

    public static class LocationUpdateRequest {
        private String region;
        private String state;
        private String city;
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    public static class HometownUpdateRequest {
        private String region;
        private String state;
        private String city;
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    public static class MajorUpdateRequest {
        private String major;
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
    }

    public static class EnrollmentUpdateRequest {
        private Integer year;
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
    }

    public static class NicknameUpdateRequest {
        private String nickname;
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
    }

    private List<ProfileOptionsVO.FacultyOptionVO> buildFacultyOptions() {
        List<ProfileOptionsVO.FacultyOptionVO> options = new ArrayList<>();
        for (int i = 0; i < OptionConstantUtils.FACULTY_OPTIONS.length; i++) {
            String[] majors = i < OptionConstantUtils.MAJOR_OPTIONS_BY_FACULTY.length
                    ? OptionConstantUtils.MAJOR_OPTIONS_BY_FACULTY[i]
                    : new String[]{"未选择"};
            options.add(new ProfileOptionsVO.FacultyOptionVO(
                    i,
                    OptionConstantUtils.FACULTY_OPTIONS[i],
                    Arrays.asList(majors)
            ));
        }
        return options;
    }

    private List<DictionaryOptionVO> buildDictionaryOptions(String[] values) {
        List<DictionaryOptionVO> options = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            options.add(new DictionaryOptionVO(i, values[i]));
        }
        return options;
    }
}
