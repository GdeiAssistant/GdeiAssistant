package edu.gdei.gdeiassistant.Controller.Privacy;

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
                //教务数据
                privacyService.UpdateCache(state, username);
                break;

            case 6:
                //搜索引擎收录
                privacyService.UpdateRobotsIndex(state, username);
                break;

            default:
                return new JsonResult(false, "请求参数不合法");
        }
        return new JsonResult(true);
    }
}
