package com.linguancheng.gdeiassistant.Controller.Wechat;

import com.linguancheng.gdeiassistant.Enum.Base.AttachResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Wechat.RequestTypeEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.Wechat.*;
import com.linguancheng.gdeiassistant.Service.Wechat.WechatService;
import com.linguancheng.gdeiassistant.Service.Wechat.WechatUserDataService;
import com.linguancheng.gdeiassistant.Tools.XMLParseUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
public class WechatController {

    private String wechatToken;

    @Value("#{propertiesReader['wechat.account.token']}")
    public void setWechatToken(String wechatToken) {
        this.wechatToken = wechatToken;
    }

    @Autowired
    private WechatUserDataService wechatUserDataService;

    @Autowired
    private WechatService wechatService;

    /**
     * 验证消息来自微信服务器
     *
     * @param wechatSignature
     * @param bindingResult
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/wechat", method = RequestMethod.GET)
    public void WechatTesting(@Validated WechatSignature wechatSignature, BindingResult bindingResult, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        if (bindingResult.hasErrors()) {
            response.getWriter().print("请求参数不合法");
        } else {
            List<String> parameterList = new ArrayList<>();
            parameterList.add(wechatToken);
            parameterList.add(wechatSignature.getTimestamp());
            parameterList.add(wechatSignature.getNonce());
            Collections.sort(parameterList);
            StringBuilder stringBuilder = new StringBuilder();
            for (String string : parameterList) {
                stringBuilder.append(string);
            }
            if (wechatSignature.getSignature().equals(DigestUtils.sha1Hex(stringBuilder.toString()))) {
                response.getWriter().print(wechatSignature.getEchostr());
            } else {
                response.getWriter().print("请求非来源于微信");
            }
        }
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * 被动接收微信公众号用户消息，返回文本结果信息
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/wechat", method = RequestMethod.POST)
    public void WechatDispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        //获取用户消息
        Map<String, String> requestMap = XMLParseUtils.ParseRequestXMLToMap(request);
        //获取发送者ID
        String fromUserName = requestMap.get("FromUserName");
        String toUserName = requestMap.get("ToUserName");
        //构建公众号回复信息
        WechatBaseMessage wechatBaseMessage = new WechatBaseMessage();
        wechatBaseMessage.setToUserName(fromUserName);
        wechatBaseMessage.setFromUserName(toUserName);
        wechatBaseMessage.setCreateTime(new Date().getTime());
        //回复的响应文本
        String responseText = null;
        //检查用户账号绑定状态
        BaseResult<String, AttachResultEnum> checkWechatAttachStateResult = wechatUserDataService
                .CheckWechatAttachState(fromUserName);
        switch (checkWechatAttachStateResult.getResultType()) {
            case ATTACHED:
                //用户已绑定账号,通过WechatID获取Username
                BaseResult<User, BoolResultEnum> queryWechatUserDataResult = wechatUserDataService
                        .QueryWechatUserData(checkWechatAttachStateResult.getResultData());
                switch (queryWechatUserDataResult.getResultType()) {
                    case SUCCESS:
                        wechatBaseMessage = wechatService.HandleUserRequest(wechatBaseMessage
                                , RequestTypeEnum.getRequestTypeEnumByFunctionName(requestMap.get("Content"))
                                , queryWechatUserDataResult.getResultData());
                        if(wechatBaseMessage instanceof WechatTextMessage){
                            responseText = XMLParseUtils.ParseWechatTextMessageToXML((WechatTextMessage) wechatBaseMessage);
                        }
                        else{
                            responseText = XMLParseUtils.ParseWechatImageTextMessageToXML((WechatImageTextMessage) wechatBaseMessage);
                        }
                        break;

                    case ERROR:
                        responseText = XMLParseUtils.ParseWechatTextMessageToXML(new WechatTextMessage(wechatBaseMessage, "服务器维护中，请稍候再试"));
                        break;
                }
                break;

            case NOT_ATTACHED:
                //用户未绑定账号，提示用户绑定账号
                responseText = XMLParseUtils.ParseWechatTextMessageToXML(new WechatTextMessage(wechatBaseMessage, "微信号未绑定教务系统账号，请点击<a href=\"https://www.gdeiassistant.cn/wechat/attach\">进入绑定</a>"+fromUserName));
                break;

            case SERVER_ERROR:
                //服务器异常
                responseText = XMLParseUtils.ParseWechatTextMessageToXML(new WechatTextMessage(wechatBaseMessage, "服务器维护中，请稍候再试"));
                break;
        }
        printWriter.print(responseText);
        printWriter.flush();
        printWriter.close();
    }
}
