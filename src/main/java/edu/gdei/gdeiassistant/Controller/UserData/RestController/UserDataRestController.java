package edu.gdei.gdeiassistant.Controller.UserData.RestController;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Enum.UserData.ExportStateEnum;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.UserData.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@RestController
public class UserDataRestController {

    @Autowired
    private UserDataService userDataService;

    /**
     * 检查用户数据导出状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/userdata/state", method = RequestMethod.GET)
    public DataJsonResult<Integer> CheckExportState(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("username");
        if (userDataService.CheckAlreadyExportUserData(username)) {
            return new DataJsonResult<>(true, ExportStateEnum.EXPORTED.getValue());
        }
        if (userDataService.CheckExportingUserData(username)) {
            return new DataJsonResult<>(true, ExportStateEnum.EXPORTING.getValue());
        }
        return new DataJsonResult<>(true, ExportStateEnum.NOT_EXPORT.getValue());
    }

    /**
     * 提交用户数据导出请求
     *
     * @param request
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/userdata/export", method = RequestMethod.POST)
    public JsonResult ExportUserData(HttpServletRequest request) throws WsgException, UnsupportedEncodingException {
        String username = (String) request.getSession().getAttribute("username");
        if (userDataService.CheckAlreadyExportUserData(username)) {
            return new JsonResult(false, "24小时内已导出过用户数据，请勿重复提交请求");
        }
        if (userDataService.CheckExportingUserData(username)) {
            return new JsonResult(false, "系统正在导出用户数据，请稍候再返回下载");
        }
        userDataService.ExportUserData(username);
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
        String username = (String) request.getSession().getAttribute("username");
        if (userDataService.CheckAlreadyExportUserData(username)) {
            String url = userDataService.DownloadUserData(username);
            return new DataJsonResult<>(true, url);
        }
        return new DataJsonResult<>(new JsonResult(false, "请先提交用户数据导出请求"));
    }
}
