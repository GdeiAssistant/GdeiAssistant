package com.gdeiassistant.gdeiassistant.Controller.SpareRoom;

import com.gdeiassistant.gdeiassistant.Annotation.QueryLogPersistence;
import com.gdeiassistant.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.SpareRoom;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.User;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseResult;
import com.gdeiassistant.gdeiassistant.Pojo.Result.DataJsonResult;
import com.gdeiassistant.gdeiassistant.Pojo.SpareRoomQuery.SpareRoomQuery;
import com.gdeiassistant.gdeiassistant.Service.SpareRoom.SpareRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class SpareRoomRestController {

    @Autowired
    private SpareRoomService spareRoomService;

    /**
     * 查询空课表信息
     *
     * @param request
     * @param spareRoomQuery
     * @return
     */
    @RequestMapping(value = "/sparequery", method = RequestMethod.POST)
    @QueryLogPersistence
    public DataJsonResult<List<SpareRoom>> QuerySpareRoomList(HttpServletRequest request
            , @Validated SpareRoomQuery spareRoomQuery) throws Exception {
        DataJsonResult<List<SpareRoom>> jsonResult = new DataJsonResult<>();
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        String password = (String) WebUtils.getSessionAttribute(request, "password");
        BaseResult<List<SpareRoom>, ServiceResultEnum> baseResult = spareRoomService
                .SyncSessionAndQuerySpareRoom(request.getSession().getId()
                        , new User(username, password), spareRoomQuery);
        switch (baseResult.getResultType()) {
            case SUCCESS:
                jsonResult.setSuccess(true);
                jsonResult.setData(baseResult.getResultData());
                break;

            case EMPTY_RESULT:
            case ERROR_CONDITION:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("没有空闲的课室");
                break;

            case TIMESTAMP_INVALID:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("时间戳校验失败，请尝试重新登录");
                break;

            case TIME_OUT:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("网络连接超时，请重试");
                break;

            case SERVER_ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("教务系统异常，请稍后再试");
                break;

            case PASSWORD_INCORRECT:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("你的密码已更新，请重新登录");
                break;
        }
        return jsonResult;
    }
}
