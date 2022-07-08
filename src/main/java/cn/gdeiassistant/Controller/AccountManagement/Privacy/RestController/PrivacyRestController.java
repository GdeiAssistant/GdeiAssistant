package cn.gdeiassistant.Controller.AccountManagement.Privacy.RestController;

import cn.gdeiassistant.Pojo.Entity.Privacy;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AccountManagement.Privacy.PrivacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PrivacyRestController {

    @Autowired
    private PrivacyService privacyService;

    /**
     * 获取用户隐私设置
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/privacy", method = RequestMethod.GET)
    public DataJsonResult<Privacy> GetUserPrivacySetting(HttpServletRequest request) throws Exception {
        Privacy privacy = privacyService.GetPrivacySetting(request.getSession().getId());
        return new DataJsonResult<>(true, privacy);
    }

    /**
     * 更新用户隐私设置
     *
     * @param request
     * @param tag
     * @param state
     * @return
     */
    @RequestMapping(value = "/api/privacy", method = RequestMethod.POST)
    public JsonResult UpdateUserPrivacySetting(HttpServletRequest request, String tag, boolean state) throws Exception {
        switch (tag.toUpperCase()) {
            case "GENDER":
                //性别
                privacyService.UpdateGender(state, request.getSession().getId());
                break;

            case "FACULTY":
                //院系
                privacyService.UpdateFaculty(state, request.getSession().getId());
                break;

            case "MAJOR":
                //专业
                privacyService.UpdateMajor(state, request.getSession().getId());
                break;

            case "LOCATION":
                //所在地
                privacyService.UpdateLocation(state, request.getSession().getId());
                break;

            case "HOMETOWN":
                //家乡
                privacyService.UpdateHometown(state, request.getSession().getId());
                break;

            case "INTRODUCTION":
                //个人简介
                privacyService.UpdateIntroduction(state, request.getSession().getId());
                break;

            case "ENROLLMENT":
                //入学年份
                privacyService.UpdateEnrollment(state, request.getSession().getId());
                break;

            case "AGE":
                //年龄
                privacyService.UpdateAge(state, request.getSession().getId());
                break;

            case "CACHE":
                //教务缓存
                privacyService.UpdateCache(state, request.getSession().getId());
                break;

            case "ROBOTS":
                //搜索引擎收录
                privacyService.UpdateRobotsIndex(state, request.getSession().getId());
                break;

            default:
                return new JsonResult(false, "请求参数不合法");
        }
        return new JsonResult(true);
    }
}
