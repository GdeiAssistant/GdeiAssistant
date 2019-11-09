package edu.gdei.gdeiassistant.Service.Wechat;

import edu.gdei.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Pojo.Entity.WechatAccount;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import edu.gdei.gdeiassistant.Service.Recognition.RecognitionService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.ImageEncodeUtils;
import edu.gdei.gdeiassistant.Tools.WechatAccountUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class WechatAccountService {

    private Log log = LogFactory.getLog(WechatAccountService.class);

    private int timeout;
    @Autowired
    private RecognitionService recognitionService;

    @Value("#{propertiesReader['timeout.wechataccount']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 定时更新微信公众号数据
     */
    @Scheduled(fixedDelay = 21600000)
    public void UpdateAccountData() {
        for (int i = 0; i < WechatAccountUtils.getWechatAccountList().size(); i++) {
            WechatAccountUtils.getWechatAccountList().get(i).setIndex(i);
            try {
                WechatAccount wechatAccount = QueryLatestWechatAccountData(null
                        , WechatAccountUtils.getWechatAccountList().get(i));
                WechatAccountUtils.getWechatAccountList().set(wechatAccount.getIndex(), wechatAccount);
            } catch (Exception ignored) {

            }
        }
    }

    /**
     * 通过搜狗微信查询微信公众号最新数据
     *
     * @param sessionId
     * @param wechatAccount
     * @return
     */
    public WechatAccount QueryLatestWechatAccountData(String sessionId, WechatAccount wechatAccount) throws NetWorkTimeoutException, ServerErrorException {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId
                    , false, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            HttpGet httpGet = new HttpGet("https://weixin.sogou.com");
            httpClient.execute(httpGet);
            httpGet = new HttpGet("https://weixin.sogou.com/weixin?query=" + wechatAccount.getId());
            httpGet.setHeader("Referer", "https://weixin.sogou.com");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Mobile Safari/537.36");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                if (document.getElementsByAttributeValue("uigs", "account_name_0").isEmpty()) {
                    Element checkcode = document.getElementById("seccodeImage");
                    Element noresult = document.getElementById("noresult_part1_container");
                    if (noresult != null) {
                        //没有相关的官方认证订阅号
                        return wechatAccount;
                    } else if (checkcode != null) {
                        //异常访问请求，进行验证码校验
                        httpGet = new HttpGet("https://weixin.sogou.com/antispider/" + checkcode.attr("src"));
                        httpResponse = httpClient.execute(httpGet);
                        InputStream inputStream = httpResponse.getEntity().getContent();
                        String checkCode = recognitionService.CheckCodeRecognize(ImageEncodeUtils
                                        .ConvertToBase64(inputStream, ImageEncodeUtils.ImageFormTypeEnum.PNG)
                                , CheckCodeTypeEnum.ENGLISH_WITH_NUMBER, 6);
                        HttpPost httpPost = new HttpPost("https://weixin.sogou.com/antispider/thank.php");
                        BasicNameValuePair basicNameValuePair1 = new BasicNameValuePair("c", checkCode);
                        BasicNameValuePair basicNameValuePair2 = new BasicNameValuePair("r", "card");
                        BasicNameValuePair basicNameValuePair3 = new BasicNameValuePair("v", "0");
                        List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
                        basicNameValuePairs.add(basicNameValuePair1);
                        basicNameValuePairs.add(basicNameValuePair2);
                        basicNameValuePairs.add(basicNameValuePair3);
                        //提交表单
                        httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
                        httpClient.execute(httpPost);
                    } else {
                        throw new ServerErrorException("通过微信号查询公众号信息异常");
                    }
                }
                String name = document.getElementsByAttributeValue("uigs", "account_name_0").text();
                String description = document.select("dl").first().select("dd").first().text();
                String article = document.getElementsByAttributeValue("uigs", "account_article_0").text();
                wechatAccount.setName(name);
                wechatAccount.setDescription(description);
                wechatAccount.setArticle(article);
                return wechatAccount;
            }
            throw new ServerErrorException("通过微信号查询公众号信息异常");
        } catch (IOException e) {
            log.error("查询公众号信息异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            log.error("查询公众号信息异常：", e);
            throw new ServerErrorException("查询公众号信息异常");
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (cookieStore != null) {
                HttpClientUtils.SyncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }
}
