package edu.gdei.gdeiassistant.Controller.Privacy.RestController;

import edu.gdei.gdeiassistant.Pojo.Entity.Privacy;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Privacy.PrivacyService;
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
    public JsonResult UpdateUserPrivacySetting(HttpServletRequest request, int index, boolean state) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        switch (index) {
            case 0:
                //性别
                privacyService.UpdateGender(state, username);
                break;

            case 1:
                //院系
                privacyService.UpdateFaculty(state, username);
                break;

            case 2:
                //专业
                privacyService.UpdateMajor(state, username);
                break;

            case 3:
                //所在地
                privacyService.UpdateLocation(state, username);
                break;

            case 4:
                //个人简介
                privacyService.UpdateIntroduction(state, username);
                break;

            case 5:
                //入学年份
                privacyService.UpdateEnrollment(state, username);
                break;

            case 6:
                //年龄
                privacyService.UpdateAge(state,username);
                break;

            case 7:
                //学历
                privacyService.UpdateDegree(state,username);
                break;

            case 8:
                privacyService.UpdateProfession(state, username);
                break;

            case 9:
                //高中/职中
                privacyService.UpdateSchool(state, 0, username);

            case 10:
                //初中
                privacyService.UpdateSchool(state, 1, username);
                break;

            case 11:
                //小学
                privacyService.UpdateSchool(state, 2, username);
                break;

            case 12:
                //教务缓存
                privacyService.UpdateCache(state, username);
                break;

            case 13:
                //搜索引擎收录
                privacyService.UpdateRobotsIndex(state, username);
                break;

            default:
                return new JsonResult(false, "请求参数不合法");
        }
        return new JsonResult(true);
    }
}
