package cn.gdeiassistant.core.privacy.controller;

import cn.gdeiassistant.common.exception.CommonException.CacheClearException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.privacy.pojo.dto.PrivacyUpdateDTO;
import cn.gdeiassistant.core.privacy.pojo.vo.PrivacyVO;
import cn.gdeiassistant.core.privacy.service.PrivacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PrivacyController {

    @Autowired
    private PrivacyService privacyService;

    /**
     * 获取用户隐私设置
     * GET /api/privacy
     */
    @RequestMapping(value = "/api/privacy", method = RequestMethod.GET)
    public DataJsonResult<PrivacyVO> getUserPrivacySetting(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        PrivacyVO privacy = privacyService.getSelfUserPrivacySetting(sessionId);
        return new DataJsonResult<>(true, privacy);
    }

    @RequestMapping(value = "/api/privacy", method = RequestMethod.POST)
    public JsonResult updateUserPrivacySetting(HttpServletRequest request, @RequestBody PrivacyUpdateDTO body) throws Exception {
        if (body == null) {
            return new JsonResult(false, "请求体不能为空");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        boolean faculty = Boolean.TRUE.equals(body.getFacultyOpen());
        boolean major = Boolean.TRUE.equals(body.getMajorOpen());
        boolean location = Boolean.TRUE.equals(body.getLocationOpen());
        boolean hometown = Boolean.TRUE.equals(body.getHometownOpen());
        boolean introduction = Boolean.TRUE.equals(body.getIntroductionOpen());
        boolean enrollment = Boolean.TRUE.equals(body.getEnrollmentOpen());
        boolean age = Boolean.TRUE.equals(body.getAgeOpen());
        boolean cache = Boolean.TRUE.equals(body.getCacheAllow());
        boolean robots = Boolean.TRUE.equals(body.getRobotsIndexAllow());

        try {
            privacyService.updateFaculty(faculty, sessionId);
            privacyService.updateMajor(major, sessionId);
            privacyService.updateLocation(location, sessionId);
            privacyService.updateHometown(hometown, sessionId);
            privacyService.updateIntroduction(introduction, sessionId);
            privacyService.updateEnrollment(enrollment, sessionId);
            privacyService.updateAge(age, sessionId);
            privacyService.updateCache(cache, sessionId);
            privacyService.updateRobotsIndex(robots, sessionId);
            return new JsonResult(true);
        } catch (CacheClearException e) {
            JsonResult result = new JsonResult(true, e.getMessage());
            result.setCode(206);
            return result;
        }
    }
}
