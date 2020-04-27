package edu.gdei.gdeiassistant.Controller.Wechat.Controller;

import edu.gdei.gdeiassistant.Enum.Wechat.RequestTypeEnum;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Wechat.WechatBaseMessage;
import edu.gdei.gdeiassistant.Pojo.Wechat.WechatImageTextMessage;
import edu.gdei.gdeiassistant.Pojo.Wechat.WechatSignature;
import edu.gdei.gdeiassistant.Pojo.Wechat.WechatTextMessage;
import edu.gdei.gdeiassistant.Service.Wechat.WechatService;
import edu.gdei.gdeiassistant.Tools.XMLParseUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(WechatController.class);

    private String wechatToken;

    @Value("#{propertiesReader['wechat.account.token']}")
    public void setWechatToken(String wechatToken) {
        this.wechatToken = wechatToken;
    }

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
        //获取用户发送的文本信息
        String content = requestMap.get("Content");
        //转发到对应的事务处理器进行处理，返回处理结果
        WechatBaseMessage resultMessage = null;
        try {
            resultMessage = wechatService.HandleUserRequest(request, wechatBaseMessage
                    , RequestTypeEnum.getRequestTypeEnumByFunctionName(content.split("-")[0])
                    , content, fromUserName);
        } catch (PasswordIncorrectException e) {
            resultMessage = new WechatTextMessage(wechatBaseMessage, "账号密码已更新，请尝试重新绑定账号");
        } catch (Exception e) {
            logger.error("微信消息对话服务异常：", e);
            resultMessage = new WechatTextMessage(wechatBaseMessage, "系统异常，请联系管理员");
        }
        if (resultMessage instanceof WechatTextMessage) {
            responseText = XMLParseUtils.ParseWechatTextMessageToXML((WechatTextMessage) resultMessage);
        } else {
            responseText = XMLParseUtils.ParseWechatImageTextMessageToXML((WechatImageTextMessage) resultMessage);
        }
        //返回响应信息
        printWriter.print(responseText);
        printWriter.flush();
        printWriter.close();
    }
}
