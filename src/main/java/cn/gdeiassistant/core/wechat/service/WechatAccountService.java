package cn.gdeiassistant.core.wechat.service;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.pojo.Entity.WechatAccount;
import cn.gdeiassistant.core.imageRecognition.service.ImageRecognitionService;
import cn.gdeiassistant.common.tools.Utils.ImageEncodeUtils;
import cn.gdeiassistant.integration.wechat.SogouWechatClient;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class WechatAccountService {

    @Autowired
    private ImageRecognitionService imageRecognitionService;

    @Autowired
    private SogouWechatClient sogouWechatClient;

    /**
     * 通过搜狗微信查询微信公众号最新数据
     *
     * @param sessionId
     * @param wechatAccount
     * @return
     */
    public WechatAccount QueryLatestWechatAccountData(String sessionId, WechatAccount wechatAccount) throws NetWorkTimeoutException, ServerErrorException {
        try {
            sogouWechatClient.fetchSogouHome(sessionId);
            Document document = sogouWechatClient.fetchWeixinSearchPage(sessionId, wechatAccount.getId());

            if (document.getElementsByAttributeValue("uigs", "account_name_0").isEmpty()) {
                Element noresult = document.getElementById("noresult_part1_container");
                if (noresult != null) {
                    return wechatAccount;
                }
                Element checkcode = document.getElementById("code-img");
                if (checkcode != null) {
                    byte[] imageBytes = sogouWechatClient.fetchAntispiderImage(sessionId, checkcode.attr("src"), wechatAccount.getId());
                    String checkCode = imageRecognitionService.CheckCodeRecognize(
                            ImageEncodeUtils.convertToBase64(new ByteArrayInputStream(imageBytes), ImageEncodeUtils.ImageFormTypeEnum.PNG),
                            CheckCodeTypeEnum.ENGLISH_WITH_NUMBER, 6);
                    String fromVal = document.getElementById("from") != null ? document.getElementById("from").val() : "";
                    String typeVal = document.getElementById("type") != null ? document.getElementById("type").val() : "";
                    document = sogouWechatClient.submitAntispiderForm(sessionId, checkCode, fromVal, typeVal, wechatAccount.getId());
                    JSONObject jsonObject = JSON.parseObject(document.body().text());
                    if (jsonObject.containsKey("code") && jsonObject.getIntValue("code") == 0) {
                        document = sogouWechatClient.fetchWeixinSearchPageAfterAntispider(sessionId, wechatAccount.getId());
                        noresult = document.getElementById("noresult_part1_container");
                        if (noresult != null) {
                            return wechatAccount;
                        }
                        String name = document.getElementsByAttributeValue("uigs", "account_name_0").text();
                        String description = document.select("dl").first().select("dd").first().text();
                        String article = document.getElementsByAttributeValue("uigs", "account_article_0").text();
                        wechatAccount.setName(name);
                        wechatAccount.setDescription(description);
                        wechatAccount.setArticle(article);
                        return wechatAccount;
                    }
                }
                throw new ServerErrorException("通过微信号查询公众号信息异常");
            }

            String name = document.getElementsByAttributeValue("uigs", "account_name_0").text();
            String description = document.select("dl").first().select("dd").first().text();
            String article = document.getElementsByAttributeValue("uigs", "account_article_0").text();
            wechatAccount.setName(name);
            wechatAccount.setDescription(description);
            wechatAccount.setArticle(article);
            return wechatAccount;
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("HTTP ")) {
                throw new ServerErrorException("通过微信号查询公众号信息异常");
            }
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            throw new ServerErrorException("查询公众号信息异常");
        }
    }
}
