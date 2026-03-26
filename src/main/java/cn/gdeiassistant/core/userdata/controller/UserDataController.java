package cn.gdeiassistant.core.userData.controller;

import cn.gdeiassistant.common.enums.UserData.ExportStateEnum;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.core.userData.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class UserDataController {

    @Autowired
    private UserDataService userDataService;

    private JsonResult failure(HttpServletRequest request, String message) {
        return new JsonResult(false, BackendTextLocalizer.localizeMessage(message, request != null ? request.getHeader("Accept-Language") : null));
    }

    /**
     * 检查用户数据导出状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/userdata/state", method = RequestMethod.GET)
    public DataJsonResult<Integer> CheckExportState(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        if (userDataService.CheckAlreadyExportUserData(sessionId)) {
            return new DataJsonResult<>(true, ExportStateEnum.EXPORTED.getValue());
        }
        if (userDataService.CheckExportingUserData(sessionId)) {
            return new DataJsonResult<>(true, ExportStateEnum.EXPORTING.getValue());
        }
        return new DataJsonResult<>(true, ExportStateEnum.NOT_EXPORT.getValue());
    }

    /**
     * 提交用户数据导出请求
     *
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/api/userdata/export", method = RequestMethod.POST)
    public JsonResult ExportUserData(HttpServletRequest request) throws IOException {
        String sessionId = (String) request.getAttribute("sessionId");
        if (userDataService.CheckAlreadyExportUserData(sessionId)) {
            return failure(request, "24小时内已导出过用户数据，请勿重复提交请求");
        }
        if (userDataService.CheckExportingUserData(sessionId)) {
            return failure(request, "系统正在导出用户数据，请稍候再返回下载");
        }
        userDataService.ExportUserData(sessionId);
        return new JsonResult(true);
    }

    /**
     * 获取下载用户数据的地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/userdata/download", method = RequestMethod.POST)
    public DataJsonResult<String> DownloadUserData(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        if (userDataService.CheckAlreadyExportUserData(sessionId)) {
            String url = userDataService.DownloadUserData(sessionId);
            return new DataJsonResult<>(true, url);
        }
        return new DataJsonResult<>(failure(request, "请先提交用户数据导出请求"));
    }
}
