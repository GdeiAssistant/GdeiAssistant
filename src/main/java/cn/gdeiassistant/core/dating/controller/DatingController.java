package cn.gdeiassistant.core.dating.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.exception.DatingException.RepeatPickException;
import cn.gdeiassistant.common.exception.DatingException.SelfPickException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPickSubmitDTO;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPublishDTO;
import cn.gdeiassistant.core.dating.pojo.vo.DatingPickVO;
import cn.gdeiassistant.core.dating.pojo.vo.DatingProfileVO;
import cn.gdeiassistant.core.dating.service.DatingService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 卖室友 REST 接口。统一 /api/dating/...（路径保留兼容），使用 Roommate* 类型。
 */
@RestController
public class DatingController {

    @Autowired
    private DatingService datingService;

    private JsonResult failure(HttpServletRequest request, String message) {
        return new JsonResult(false, BackendTextLocalizer.localizeMessage(message, request != null ? request.getHeader("Accept-Language") : null));
    }

    @RequestMapping(value = "/api/dating/profile/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<Map<String, Object>> getRoommateProfileDetail(HttpServletRequest request, @PathVariable("id") Integer id) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        DatingProfileVO profile = datingService.queryDatingProfile(id);
        String pictureURL = datingService.getRoommateProfilePictureURL(id);
        DatingPickVO pick = datingService.queryRoommatePick(id, sessionId);
        boolean isContactVisible = false, isPickNotAvailable = false;
        if (pick != null) {
            isPickNotAvailable = datingService.checkIsPickPageHidden(sessionId, pick.getPickId());
            isContactVisible = isPickNotAvailable;
        }
        if (!isContactVisible) {
            profile.setQq(null);
            profile.setWechat(null);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("profile", profile);
        data.put("pictureURL", pictureURL);
        data.put("isContactVisible", isContactVisible);
        data.put("isPickNotAvailable", isPickNotAvailable);
        return new DataJsonResult<>(true, data);
    }

    @RequestMapping(value = "/api/dating/profile/area/{area}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<DatingProfileVO>> queryDatingProfile(@PathVariable("start") Integer start, @PathVariable("area") Integer area) {
        List<DatingProfileVO> list = datingService.queryDatingProfile(start, 10, area);
        for (DatingProfileVO p : list) {
            if (p.getProfileId() != null) p.setPictureURL(datingService.getRoommateProfilePictureURL(p.getProfileId()));
        }
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/dating/profile/id/{id}/picture", method = RequestMethod.GET)
    public DataJsonResult<String> getRoommateProfilePicture(@PathVariable("id") Integer id) {
        String url = datingService.getRoommateProfilePictureURL(id);
        return StringUtils.isNotBlank(url) ? new DataJsonResult<>(true, url) : new DataJsonResult<>(new JsonResult(false));
    }

    @RequestMapping(value = "/api/dating/profile/my", method = RequestMethod.GET)
    public DataJsonResult<List<DatingProfileVO>> getMyRoommateProfiles(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, datingService.queryMyRoommateProfiles(sessionId));
    }

    @RequestMapping(value = "/api/dating/pick/my/sent", method = RequestMethod.GET)
    public DataJsonResult<List<DatingPickVO>> getMySentPicks(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, datingService.queryMySentPicks(sessionId));
    }

    @RequestMapping(value = "/api/dating/pick/my/received", method = RequestMethod.GET)
    public DataJsonResult<List<DatingPickVO>> getMyReceivedPicks(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, datingService.queryMyReceivedPicks(sessionId));
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @RequestMapping(value = "/api/dating/profile", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addRoommateProfile(HttpServletRequest request, @Validated DatingPublishDTO dto,
                                         MultipartFile image,
                                         String imageKey) throws IOException {
        String sessionId = (String) request.getAttribute("sessionId");
        Integer id = datingService.addRoommateProfile(sessionId, dto);
        try {
            if (image != null && image.getSize() > 0 && image.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                datingService.uploadPicture(id, image.getInputStream());
            } else if (StringUtils.isNotBlank(imageKey)) {
                datingService.movePictureFromTempObject(id, imageKey);
            }
        } catch (Exception e) {
            datingService.deleteDatingImage(id);
            datingService.deleteDatingProfile(id);
            return failure(request, "上传失败");
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/dating/pick/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<DatingPickVO> getRoommatePickDetail(HttpServletRequest request, @PathVariable("id") Integer id) throws DataNotExistException, NoAccessException {
        datingService.verifyRoommatePickViewAccess((String) request.getAttribute("sessionId"), id);
        DatingPickVO vo = datingService.getRoommatePickDetailVo(id);
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/dating/pick/id/{id}", method = RequestMethod.POST)
    public JsonResult updateRoommatePickState(HttpServletRequest request, @PathVariable("id") Integer id, Integer state) throws DataNotExistException, NoAccessException {
        if (state == null || (!state.equals(-1) && !state.equals(1))) return failure(request, "请求参数不合法");
        String sessionId = (String) request.getAttribute("sessionId");
        datingService.verifyRoommatePickViewAccess(sessionId, id);
        datingService.updateRoommatePickState(id, state);
        return new JsonResult(true);
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @RequestMapping(value = "/api/dating/pick", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addRoommatePick(HttpServletRequest request, @Validated DatingPickSubmitDTO dto) throws SelfPickException, RepeatPickException, DataNotExistException {
        if (dto.getProfileId() == null) return failure(request, "请求参数不合法");
        if (dto.getContent() != null && dto.getContent().length() > 50) return failure(request, "文本内容超过限制");
        String sessionId = (String) request.getAttribute("sessionId");
        datingService.verifyRoommatePickRequestAccess(sessionId, dto.getProfileId());
        datingService.addRoommatePick(sessionId, dto);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/dating/profile/id/{id}/state", method = RequestMethod.POST)
    public JsonResult updateMyRoommateProfileState(HttpServletRequest request, @PathVariable("id") Integer id, Integer state) throws DataNotExistException, NoAccessException {
        String sessionId = (String) request.getAttribute("sessionId");
        datingService.verifyRoommateProfileOwner(sessionId, id);
        datingService.updateRoommateProfileState(id, state);
        return new JsonResult(true);
    }
}
