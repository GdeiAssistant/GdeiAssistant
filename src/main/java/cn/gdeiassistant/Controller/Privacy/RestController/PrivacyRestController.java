package cn.gdeiassistant.Controller.Privacy.RestController;

import cn.gdeiassistant.Pojo.Entity.Privacy;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Privacy.PrivacyService;
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
        String username = (String) request.getSession().getAttribute("username");
        Privacy privacy = privacyService.GetPrivacySetting(username);
        return new DataJsonResult<>(true, privacy);
    }

    /**
     * 更新用户隐私设置
     *
     * @param request
     * @param index
     * @param state
     * @return
     */
    @RequestMapping(value = "/api/privacy", method = RequestMethod.POST)
    public JsonResult UpdateUserPrivacySetting(HttpServletRequest request, String index, boolean state) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        switch (index.toUpperCase()) {
            case "GENDER":
                //性别
                privacyService.UpdateGender(state, username);
                break;

            case "FACULTY":
                //院系
                privacyService.UpdateFaculty(state, username);
                break;

            case "MAJOR":
                //专业
                privacyService.UpdateMajor(state, username);
                break;

            case "LOCATION":
                //所在地
                privacyService.UpdateLocation(state, username);
                break;

            case "HOMETOWN":
                //家乡
                privacyService.UpdateHometown(state, username);
                break;

            case "INTRODUCTION":
                //个人简介
                privacyService.UpdateIntroduction(state, username);
                break;

            case "ENROLLMENT":
                //入学年份
                privacyService.UpdateEnrollment(state, username);
                break;

            case "AGE":
                //年龄
                privacyService.UpdateAge(state, username);
                break;

            case "CACHE":
                //教务缓存
                privacyService.UpdateCache(state, username);
                break;

            case "ROBOTS_INDEX":
                //搜索引擎收录
                privacyService.UpdateRobotsIndex(state, username);
                break;

            default:
                return new JsonResult(false, "请求参数不合法");
        }
        return new JsonResult(true);
    }
}
