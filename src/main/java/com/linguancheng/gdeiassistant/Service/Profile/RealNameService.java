package com.linguancheng.gdeiassistant.Service.Profile;

import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Pojo.Entity.CardInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.CardQuery.CardQueryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.web.HttpSessionRequiredException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class RealNameService {

    private String url;

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Autowired
    private CardQueryService cardQueryService;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }

    private Log log = LogFactory.getLog(RealNameService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.realname']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 获取用户真实姓名（弃用）
     *
     * @param request
     * @param user
     * @return
     */
    @Deprecated
    public BaseResult<String, BoolResultEnum> GetUserRealName(HttpServletRequest request, User user) {
        BaseResult<String, BoolResultEnum> result = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            HttpGet httpGet = new HttpGet(url + "cas_verify.aspx?i=" + user.getUsername() + "&k=" + user.getKeycode());
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().isEmpty()) {
                httpGet = new HttpGet(url + "xs_main.aspx?xh=" + user.getNumber());
                httpResponse = httpClient.execute(httpGet);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("正方教务管理系统")) {
                    //获取学生的身份证号
                    httpGet = new HttpGet(url + "xsgrxx.aspx?xh=" + user.getNumber());
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("现代教学管理信息系统")) {
                        String pin = document.getElementById("lbl_sfzh").text();
                        //获取身份证后六位数字
                        if (pin.substring(pin.length() - 1, pin.length()).equals("X")) {
                            //最后一位不是数字
                            pin = pin.substring(pin.length() - 7, pin.length() - 1);
                        } else {
                            pin = pin.substring(pin.length() - 6, pin.length());
                        }
                        //获取用户姓名
                        HttpPost httpPost = new HttpPost("http://cas.gdei.edu.cn:6060/ucenter/query_account_result.jsp");
                        List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                        basicNameValuePairList.add(new BasicNameValuePair("querytype", "XH"));
                        basicNameValuePairList.add(new BasicNameValuePair("querykey", user.getNumber()));
                        basicNameValuePairList.add(new BasicNameValuePair("identify", pin));
                        basicNameValuePairList.add(new BasicNameValuePair("usertype", "student"));
                        basicNameValuePairList.add(new BasicNameValuePair("Submit", "查询"));
                        httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                        httpResponse = httpClient.execute(httpPost);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            //获取学校姓名
                            Element tdcolor = document.getElementsByClass("tdcolor").first();
                            String xhxm = tdcolor.select("td").get(1).text();
                            //缓存学生的信息
                            result.setResultData(xhxm);
                            result.setResultType(BoolResultEnum.SUCCESS);
                            return result;
                        }
                        throw new ServerErrorException("教务系统异常");
                    }
                    throw new ServerErrorException("教务系统异常");
                }
                throw new ServerErrorException("教务系统异常");
            }
            throw new ServerErrorException("教务系统异常");
        } catch (Exception e) {
            log.error("获取用户真实姓名异常：" , e);
            result.setResultType(BoolResultEnum.ERROR);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 获取用户真实姓名（新方法）
     * 该方法调用校园卡基本信息查询的模块获取姓名信息
     *
     * @param request
     * @param username
     * @param password
     * @return
     */
    public BaseResult<String, BoolResultEnum> GetUserRealName(HttpServletRequest request, String username, String password) {
        BaseResult<String, BoolResultEnum> result = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            //登录支付管理平台
            cardQueryService.LoginCardSystem(httpClient, username, password);
            //获取校园卡基本信息
            CardInfo cardInfo = cardQueryService.QueryCardInformation(httpClient);
            result.setResultData(cardInfo.getName());
            result.setResultType(BoolResultEnum.SUCCESS);
        } catch (Exception e) {
            log.error("获取用户真实姓名异常：" , e);
            result.setResultType(BoolResultEnum.ERROR);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
