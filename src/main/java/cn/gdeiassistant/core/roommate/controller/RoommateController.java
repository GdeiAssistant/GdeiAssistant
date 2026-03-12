package cn.gdeiassistant.core.roommate.controller;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.exception.DatingException.RepeatPickException;
import cn.gdeiassistant.common.exception.DatingException.SelfPickException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.roommate.pojo.dto.RoommatePickSubmitDTO;
import cn.gdeiassistant.core.roommate.pojo.dto.RoommatePublishDTO;
import cn.gdeiassistant.core.roommate.pojo.vo.RoommatePickVO;
import cn.gdeiassistant.core.roommate.pojo.vo.RoommateProfileVO;
import cn.gdeiassistant.core.roommate.service.RoommateService;
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
public class RoommateController {

    @Autowired
    private RoommateService roommateService;

    @RequestMapping(value = "/api/dating/profile/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<Map<String, Object>> getRoommateProfileDetail(HttpServletRequest request, @PathVariable("id") Integer id) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        RoommateProfileVO profile = roommateService.queryRoommateProfile(id);
        String pictureURL = roommateService.getRoommateProfilePictureURL(id);
        RoommatePickVO pick = roommateService.queryRoommatePick(id, sessionId);
        boolean isContactVisible = false, isPickNotAvailable = false;
        if (pick != null) {
            isPickNotAvailable = roommateService.checkIsPickPageHidden(sessionId, pick.getPickId());
            isContactVisible = isPickNotAvailable;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("profile", profile);
        data.put("pictureURL", pictureURL);
        data.put("isContactVisible", isContactVisible);
        data.put("isPickNotAvailable", isPickNotAvailable);
        return new DataJsonResult<>(true, data);
    }

    @RequestMapping(value = "/api/dating/profile/area/{area}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<RoommateProfileVO>> queryRoommateProfile(@PathVariable("start") Integer start, @PathVariable("area") Integer area) {
        List<RoommateProfileVO> list = roommateService.queryRoommateProfile(start, 10, area);
        for (RoommateProfileVO p : list) {
            if (p.getProfileId() != null) p.setPictureURL(roommateService.getRoommateProfilePictureURL(p.getProfileId()));
        }
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/dating/profile/id/{id}/picture", method = RequestMethod.GET)
    public DataJsonResult<String> getRoommateProfilePicture(@PathVariable("id") Integer id) {
        String url = roommateService.getRoommateProfilePictureURL(id);
        return StringUtils.isNotBlank(url) ? new DataJsonResult<>(true, url) : new DataJsonResult<>(new JsonResult(false));
    }

    @RequestMapping(value = "/api/dating/profile/my", method = RequestMethod.GET)
    public DataJsonResult<List<RoommateProfileVO>> getMyRoommateProfiles(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, roommateService.queryMyRoommateProfiles(sessionId));
    }

    @RequestMapping(value = "/api/dating/pick/my/sent", method = RequestMethod.GET)
    public DataJsonResult<List<RoommatePickVO>> getMySentPicks(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, roommateService.queryMySentPicks(sessionId));
    }

    @RequestMapping(value = "/api/dating/pick/my/received", method = RequestMethod.GET)
    public DataJsonResult<List<RoommatePickVO>> getMyReceivedPicks(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, roommateService.queryMyReceivedPicks(sessionId));
    }

    @RequestMapping(value = "/api/dating/profile", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addRoommateProfile(HttpServletRequest request, @Validated RoommatePublishDTO dto,
                                         MultipartFile image,
                                         String imageKey) throws IOException {
        String sessionId = (String) request.getAttribute("sessionId");
        Integer id = roommateService.addRoommateProfile(sessionId, dto);
        if (image != null && image.getSize() > 0 && image.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
            roommateService.uploadPicture(id, image.getInputStream());
        } else if (StringUtils.isNotBlank(imageKey)) {
            roommateService.movePictureFromTempObject(id, imageKey);
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/dating/pick/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<RoommatePickVO> getRoommatePickDetail(HttpServletRequest request, @PathVariable("id") Integer id) throws DataNotExistException, NoAccessException {
        roommateService.verifyRoommatePickViewAccess((String) request.getAttribute("sessionId"), id);
        RoommatePickVO vo = roommateService.getRoommatePickDetailVo(id);
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/dating/pick/id/{id}", method = RequestMethod.POST)
    public JsonResult updateRoommatePickState(HttpServletRequest request, @PathVariable("id") Integer id, Integer state) throws DataNotExistException, NoAccessException {
        if (state == null || (!state.equals(-1) && !state.equals(1))) return new JsonResult(false, "请求参数不合法");
        String sessionId = (String) request.getAttribute("sessionId");
        roommateService.verifyRoommatePickViewAccess(sessionId, id);
        roommateService.updateRoommatePickState(id, state);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/dating/pick", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addRoommatePick(HttpServletRequest request, @Validated RoommatePickSubmitDTO dto) throws SelfPickException, RepeatPickException {
        if (dto.getProfileId() == null) return new JsonResult(false, "请求参数不合法");
        if (dto.getContent() != null && dto.getContent().length() > 50) return new JsonResult(false, "文本内容超过限制");
        String sessionId = (String) request.getAttribute("sessionId");
        roommateService.verifyRoommatePickRequestAccess(sessionId, dto.getProfileId());
        roommateService.addRoommatePick(sessionId, dto);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/dating/profile/id/{id}/state", method = RequestMethod.POST)
    public JsonResult updateMyRoommateProfileState(HttpServletRequest request, @PathVariable("id") Integer id, Integer state) throws DataNotExistException, NoAccessException {
        String sessionId = (String) request.getAttribute("sessionId");
        roommateService.verifyRoommateProfileOwner(sessionId, id);
        roommateService.updateRoommateProfileState(id, state);
        return new JsonResult(true);
    }
}
