package com.linguancheng.gdeiassistant.Controller.SpareRoom;

import com.linguancheng.gdeiassistant.Annotation.QueryLog;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLog;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.SpareRoom;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.SpareRoomQuery.SpareRoomQuery;
import com.linguancheng.gdeiassistant.Service.SpareRoom.SpareRoomService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class SpareRoomController {

    @Autowired
    private SpareRoomService spareRoomService;

    @Autowired
    private UserLoginService userLoginService;

    /**
     * 进入空课表查询页面
     *
     * @return
     */
    @RequestMapping(value = "/spare")
    public ModelAndView ResolveSparePage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Spare/spare");
        //获取可用的日期列表
        LocalDate localDate = LocalDate.now();
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            if (i == 0) {
                Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Date date = Date.from(instant);
                dateList.add(date);
            } else {
                localDate = localDate.plusDays(1);
                Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Date date = Date.from(instant);
                dateList.add(date);
            }
        }
        modelAndView.addObject("DateList", dateList);
        return modelAndView;
    }

    /**
     * 查询空课表信息
     *
     * @param request
     * @param spareRoomQuery
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/sparequery", method = RequestMethod.POST)
    @QueryLog
    @ResponseBody
    public DataJsonResult<List<SpareRoom>> QuerySpareRoomList(HttpServletRequest request
            , @Validated SpareRoomQuery spareRoomQuery, BindingResult bindingResult) {
        DataJsonResult<List<SpareRoom>> jsonResult = new DataJsonResult<>();
        if (bindingResult.hasErrors()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数不合法");
        } else {
            String username = (String) WebUtils.getSessionAttribute(request, "username");
            //检测是否已与教务系统进行会话同步
            if (request.getSession().getAttribute("timestamp") != null) {
                //进行会话同步
                switch (userLoginService.SyncUpdateSession(request)) {
                    case SUCCESS:
                        break;

                    case TIME_OUT:
                        //连接超时
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("网络连接超时，请重试");
                        return jsonResult;

                    case PASSWORD_INCORRECT:
                        //身份凭证异常
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("用户凭证已过期，请重新登录");
                        return jsonResult;

                    default:
                        //服务器异常
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("学院教务系统维护中，暂不可用");
                        return jsonResult;
                }
            }
            String keycode = (String) WebUtils.getSessionAttribute(request, "keycode");
            String number = (String) WebUtils.getSessionAttribute(request, "number");
            Long timestamp = (Long) WebUtils.getSessionAttribute(request, "timestamp");
            if (!StringUtils.isBlank(username) && !StringUtils.isBlank(keycode) && !StringUtils.isBlank(number)) {
                BaseResult<List<SpareRoom>, ServiceResultEnum> baseResult = spareRoomService
                        .QuerySpareRoom(request, username, keycode, number, timestamp
                                , spareRoomQuery);
                switch (baseResult.getResultType()) {
                    case SUCCESS:
                        jsonResult.setSuccess(true);
                        jsonResult.setData(baseResult.getResultData());
                        break;

                    case EMPTY_RESULT:
                    case ERROR_CONDITION:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("没有空闲的课室");
                        break;

                    case TIMESTAMP_INVALID:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("时间戳校验失败，请尝试重新登录");
                        break;

                    case TIME_OUT:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("网络连接超时，请重试");
                        break;

                    case SERVER_ERROR:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("教务系统异常，请稍后再试");
                        break;

                    case PASSWORD_INCORRECT:
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("你的密码已更新，请重新登录");
                        break;
                }
            } else {
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("用户身份凭证已过期，请重新登录");
            }
        }
        return jsonResult;
    }
}