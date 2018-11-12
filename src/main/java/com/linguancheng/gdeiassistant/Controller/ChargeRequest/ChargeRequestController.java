package com.linguancheng.gdeiassistant.Controller.ChargeRequest;

import com.linguancheng.gdeiassistant.Enum.Charge.ChargeRequestResultEnum;
import com.linguancheng.gdeiassistant.Enum.Charge.GetServerKeyCodeResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Charge.*;
import com.linguancheng.gdeiassistant.Pojo.Entity.Charge;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.ChargeQuery.ChargeService;
import com.linguancheng.gdeiassistant.Tools.HttpClientUtils;
import com.linguancheng.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ChargeRequestController {

    @Autowired
    private ChargeService chargeService;

    @RequestMapping(value = "/rest/charge", method = RequestMethod.POST)
    @ResponseBody
    public ChargeRequestJsonResult ChargeRequest() {
        ChargeRequestJsonResult chargeRequestJsonResult = new ChargeRequestJsonResult();
        chargeRequestJsonResult.setSuccess(false);
        chargeRequestJsonResult.setMessage("校园卡充值功能已关闭");
        return chargeRequestJsonResult;
    }

    @Deprecated
//    @RequestMapping(value = "/rest/charge", method = RequestMethod.POST)
//    @ResponseBody
    public ChargeRequestJsonResult ChargeRequest(HttpServletRequest request, ChargeSecurity chargeSecurity
            , @Validated(UserLoginValidGroup.class) User user
            , @Validated ChargeRequest chargeRequest, BindingResult bindingResult) {
        ChargeRequestJsonResult chargeRequestJsonResult = new ChargeRequestJsonResult();
        if (bindingResult.hasErrors()) {
            chargeRequestJsonResult.setSuccess(false);
            chargeRequestJsonResult.setMessage("请求参数不合法");
            return chargeRequestJsonResult;
        }
        if (request.getSession() == null) {
            chargeRequestJsonResult.setSuccess(false);
            chargeRequestJsonResult.setMessage("用户身份凭证过期,请重新打开充值页面");
            return chargeRequestJsonResult;
        } else {
            //判断是否需要做安全校验
            if (chargeSecurity.isSecurityRequired()) {
                switch (chargeService.VerifyClientKeyCode(chargeSecurity.getClientKeyCode(), chargeSecurity.getTimeStamp())) {
                    case VERIFY_SUCCESS:
                        if (chargeSecurity.isTimeStampExpired()) {
                            chargeRequestJsonResult.setSuccess(false);
                            chargeRequestJsonResult.setMessage("当前系统时间不正确,请校正后重试");
                            return chargeRequestJsonResult;
                        }
                        break;

                    case VERIFY_FAILURE:
                        chargeRequestJsonResult.setSuccess(false);
                        chargeRequestJsonResult.setMessage("非法的客户端请求");
                        return chargeRequestJsonResult;

                    case VERIFY_EXCEPTION:
                        chargeRequestJsonResult.setSuccess(false);
                        chargeRequestJsonResult.setMessage("饭卡充值系统维护中,请稍候再试");
                        return chargeRequestJsonResult;
                }
            }
            BaseResult<Charge, ChargeRequestResultEnum> result = chargeService
                    .ChargeRequest(request.getSession().getId(), user.getUsername(), user.getPassword()
                            , chargeRequest.getAmount());
            switch (result.getResultType()) {
                case SERVER_ERROR:
                    //服务器异常
                    chargeRequestJsonResult.setSuccess(false);
                    chargeRequestJsonResult.setMessage("饭卡充值系统维护中,请稍候再试");
                    break;

                case REQUEST_EXPIRED:
                    //用户身份凭证过期
                    chargeRequestJsonResult.setSuccess(false);
                    chargeRequestJsonResult.setMessage("用户身份凭证过期,请重新打开充值页面");
                    break;

                case ACCOUNT_NOT_AVAILABLE:
                    //充值金额不合法
                    chargeRequestJsonResult.setSuccess(false);
                    chargeRequestJsonResult.setMessage("充值金额不合法");
                    break;

                case INCONSISTENT_INFORMATION:
                    //校验用户姓名信息出现不一致的安全问题
                    chargeRequestJsonResult.setSuccess(false);
                    chargeRequestJsonResult.setMessage("支付环境不安全,已停止交易");
                    break;

                case REQUEST_SUCCESS:
                    //判断是否需要返回服务端Keycode
                    if (chargeSecurity.isSecurityRequired()) {
                        //获取供客户端校验的服务端Keycode
                        BaseResult<String, GetServerKeyCodeResultEnum> serverKeyCodeResult = chargeService.GetServerKeycode(user, chargeSecurity.getSecurityVersion()
                                , chargeSecurity.getClientType(), chargeSecurity.getTimeStamp(), chargeRequest.getAmount());
                        switch (serverKeyCodeResult.getResultType()) {
                            case GETKEYCODE_SUCCESS:
                                //保存充值记录
                                chargeService.SaveChargeLog(user.getUsername(), chargeRequest.getAmount());
                                chargeRequestJsonResult.setSuccess(true);
                                chargeRequestJsonResult.setCharge(result.getResultData());
                                chargeRequestJsonResult.setServerKeycode(serverKeyCodeResult.getResultData());
                                break;

                            case NULL_USERINFORMATION:
                            case INCORRECT_USERINFORMATION:
                                chargeRequestJsonResult.setSuccess(false);
                                chargeRequestJsonResult.setMessage("请求参数不合法");
                                break;

                            case UNSUPPORT_CLIENTTYPE:
                                chargeRequestJsonResult.setSuccess(false);
                                chargeRequestJsonResult.setMessage("不支持当前客户端类型");
                                break;

                            case UNSUPPORT_SECURITYVERSION:
                                chargeRequestJsonResult.setSuccess(false);
                                chargeRequestJsonResult.setMessage("不支持当前安全校验版本");
                                break;
                        }
                    } else {
                        //保存充值记录
                        chargeService.SaveChargeLog(user.getUsername(), chargeRequest.getAmount());
                        //充值请求提交成功
                        chargeRequestJsonResult.setSuccess(true);
                        chargeRequestJsonResult.setCharge(result.getResultData());
                    }
                    //清除请求缓存的Cookie
                    HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
                    break;
            }
            return chargeRequestJsonResult;
        }
    }
}