package cn.gdeiassistant.core.userProfile.controller;

import cn.gdeiassistant.common.pojo.Entity.Region;
import cn.gdeiassistant.common.pojo.Entity.Introduction;
import cn.gdeiassistant.common.exception.CommonException.FeatureNotEnabledException;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.profile.pojo.LocationComparator;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.userProfile.controller.mapper.ProfileResponseMapper;
import cn.gdeiassistant.core.userProfile.controller.request.*;
import cn.gdeiassistant.core.userProfile.controller.support.ProfileLocationValidator;
import cn.gdeiassistant.core.userProfile.pojo.ProfileOptionsVO;
import cn.gdeiassistant.core.userProfile.pojo.UserProfileVO;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userProfile.service.ProfileOptionsFacade;
import cn.gdeiassistant.common.tools.Utils.LocationUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.web.util.HtmlUtils;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ProfileResponseMapper profileResponseMapper;

    @Autowired
    private ProfileOptionsFacade profileOptionsFacade;

    @Autowired
    private ProfileLocationValidator profileLocationValidator;

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
        UserProfileVO vo = profileResponseMapper.toUserProfileVO(profile, sessionId);
        DataJsonResult<UserProfileVO> result = new DataJsonResult<>(true, vo);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    /**
     * 更新用户个人简介
     *
     * @param request
     * @param body
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/api/introduction", method = RequestMethod.POST)
    public JsonResult UpdateIntroduction(HttpServletRequest request, @RequestBody IntroductionUpdateRequest body) throws Exception {
        String introduction = body != null ? body.getIntroduction() : null;
        if (introduction != null && introduction.length() <= 80) {
            if (introduction.isEmpty()) {
                introduction = null;
            } else {
                introduction = HtmlUtils.htmlEscape(introduction);
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
        return new DataJsonResult<>(true, profileOptionsFacade.buildProfileOptions());
    }

    /**
     * 更新生日日期
     *
     * @param request
     * @param body
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
     * @param body
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
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/location", method = RequestMethod.POST)
    public JsonResult UpdateLocation(HttpServletRequest request, @RequestBody LocationUpdateRequest body) throws Exception {
        if (body == null || StringUtils.isBlank(body.getRegion())) {
            return new JsonResult(false, "不合法的国家/地区代码");
        }
        ProfileLocationValidator.ValidationResult vr = profileLocationValidator.validate(
                HtmlUtils.htmlEscape(body.getRegion()),
                body.getState() != null ? HtmlUtils.htmlEscape(body.getState()) : null,
                body.getCity() != null ? HtmlUtils.htmlEscape(body.getCity()) : null);
        if (!vr.isValid()) {
            return new JsonResult(false, vr.getErrorMessage());
        }
        userProfileService.updateLocation(
                (String) request.getAttribute("sessionId"), vr.getRegion(), vr.getState(), vr.getCity());
        return new JsonResult(true);
    }

    /**
     * 更新用户家乡
     *
     * @param request
     * @param body
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/profile/hometown", method = RequestMethod.POST)
    public JsonResult UpdateHometown(HttpServletRequest request, @RequestBody HometownUpdateRequest body) throws Exception {
        if (body == null || StringUtils.isBlank(body.getRegion())) {
            return new JsonResult(false, "不合法的国家/地区代码");
        }
        ProfileLocationValidator.ValidationResult vr = profileLocationValidator.validate(
                HtmlUtils.htmlEscape(body.getRegion()),
                body.getState() != null ? HtmlUtils.htmlEscape(body.getState()) : null,
                body.getCity() != null ? HtmlUtils.htmlEscape(body.getCity()) : null);
        if (!vr.isValid()) {
            return new JsonResult(false, vr.getErrorMessage());
        }
        userProfileService.updateHometown(
                (String) request.getAttribute("sessionId"), vr.getRegion(), vr.getState(), vr.getCity());
        return new JsonResult(true);
    }

    /**
     * 更新用户专业
     *
     * @param request
     * @param body
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/api/profile/major", method = RequestMethod.POST)
    public JsonResult UpdateMajor(HttpServletRequest request, @RequestBody MajorUpdateRequest body) throws Exception {
        String major = body != null ? body.getMajor() : null;
        if (major == null || major.isEmpty() || major.length() > 20) {
            return new JsonResult(false, "专业长度不合法");
        }
        major = HtmlUtils.htmlEscape(major);
        userProfileService.updateMajor((String) request.getAttribute("sessionId"), major);
        return new JsonResult(true);
    }

    /**
     * 更新入学年份
     *
     * @param request
     * @param body
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
     * @param body
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/api/profile/nickname", method = RequestMethod.POST)
    public JsonResult UpdateNickname(HttpServletRequest request, @RequestBody NicknameUpdateRequest body) throws Exception {
        String nickname = body != null ? body.getNickname() : null;
        if (nickname == null || nickname.isEmpty() || nickname.length() > 32) {
            return new JsonResult(false, "昵称长度不合法");
        }
        nickname = HtmlUtils.htmlEscape(nickname);
        userProfileService.updateNickname((String) request.getAttribute("sessionId"), nickname);
        return new JsonResult(true);
    }

    /**
     * 获取昵称信息接口
     *
     * @param request
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
}
