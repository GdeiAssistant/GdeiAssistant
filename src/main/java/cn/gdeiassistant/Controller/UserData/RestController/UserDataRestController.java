package cn.gdeiassistant.Controller.UserData.RestController;

import com.taobao.wsgsvr.WsgException;
import cn.gdeiassistant.Enum.UserData.ExportStateEnum;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.UserData.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
        if (userDataService.CheckAlreadyExportUserData(request.getSession().getId())) {
            return new DataJsonResult<>(true, ExportStateEnum.EXPORTED.getValue());
        }
        if (userDataService.CheckExportingUserData(request.getSession().getId())) {
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
    public JsonResult ExportUserData(HttpServletRequest request) throws WsgException, IOException {
        if (userDataService.CheckAlreadyExportUserData(request.getSession().getId())) {
            return new JsonResult(false, "24小时内已导出过用户数据，请勿重复提交请求");
        }
        if (userDataService.CheckExportingUserData(request.getSession().getId())) {
            return new JsonResult(false, "系统正在导出用户数据，请稍候再返回下载");
        }
        userDataService.ExportUserData(request.getSession().getId());
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
        if (userDataService.CheckAlreadyExportUserData(request.getSession().getId())) {
            String url = userDataService.DownloadUserData(request.getSession().getId());
            return new DataJsonResult<>(true, url);
        }
        return new DataJsonResult<>(new JsonResult(false, "请先提交用户数据导出请求"));
    }
}
