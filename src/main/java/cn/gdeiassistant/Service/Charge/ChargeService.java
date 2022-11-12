package cn.gdeiassistant.Service.AcademicAffairs.Charge;

import cn.gdeiassistant.Exception.ChargeException.AmountNotAvailableException;
import cn.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Exception.CommonException.ServerErrorException;
import cn.gdeiassistant.Pojo.Entity.Charge;
import cn.gdeiassistant.Pojo.Entity.ChargeLog;
import cn.gdeiassistant.Pojo.Entity.Cookie;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import cn.gdeiassistant.Pojo.UserLogin.UserCertificate;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.Charge.ChargeMapper;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.Utils.HttpClientUtils;
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
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChargeService {

    private final Logger logger = LoggerFactory.getLogger(ChargeService.class);

    @Autowired
    private ChargeMapper chargeMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    /**
     * 提交校园卡充值请求并自动确认，返回支付宝URL和Cookie列表
     *
     * @param sessionId
     * @param amount
     * @return
     */
    public Charge ChargeRequest(String sessionId, int amount) throws Exception {
        UserCertificate userCertificate = userCertificateService.GetUserSessionCertificate(sessionId);
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            if (amount <= 0 || amount > 500) {
                throw new AmountNotAvailableException("充值金额超过范围");
            }
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, 15);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //登录支付管理平台
            LoginCardSystem(httpClient, userCertificate.getUser().getUsername()
                    , userCertificate.getUser().getPassword(), false);
            //发送充值请求
            Map<String, String> ecardDataMap = SendChargeRequest(httpClient, amount);
            //确认充值请求
            return ConfirmChargeRequest(httpClient, cookieStore, ecardDataMap);
        } catch (AmountNotAvailableException e) {
            logger.error("校园卡充值异常：", e);
            throw new AmountNotAvailableException("用户充值金额超过范围");
        } catch (IOException e) {
            logger.error("校园卡充值异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("校园卡充值异常：", e);
            throw new ServerErrorException("支付管理系统异常");
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

    /**
     * 登录支付管理平台
     *
     * @param httpClient
     * @param username
     * @param password
     * @param autoRedirect
     * @throws ServerErrorException
     * @throws IOException
     * @throws PasswordIncorrectException
     */
    private void LoginCardSystem(CloseableHttpClient httpClient, String username, String password, boolean autoRedirect) throws ServerErrorException, IOException, PasswordIncorrectException {
        HttpGet httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.getElementsByClass("pcclient").size() > 0) {
            //封装需要提交的数据
            List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
            basicNameValuePairs.add(new BasicNameValuePair("imageField.x", "0"));
            basicNameValuePairs.add(new BasicNameValuePair("imageField.y", "0"));
            basicNameValuePairs.add(new BasicNameValuePair("username", username));
            basicNameValuePairs.add(new BasicNameValuePair("password", password));
            basicNameValuePairs.add(new BasicNameValuePair("service", "http://ecard.gdei.edu.cn:8050/LoginCas.aspx"));
            basicNameValuePairs.add(new BasicNameValuePair("tokens", document.getElementById("tokens").val()));
            basicNameValuePairs.add(new BasicNameValuePair("stamp", document.getElementById("stamp").val()));
            HttpPost httpPost = new HttpPost("https://security.gdei.edu.cn/cas/login");
            //绑定表单参数
            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
            httpResponse = httpClient.execute(httpPost);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                if (document.getElementsByClass("pcclient").size() > 0) {
                    throw new PasswordIncorrectException("用户名或密码错误");
                }
                //获取html页面中的首个URL地址,进入支付系统页面
                httpGet = new HttpGet(document.select("a").first().attr("href"));
                //请求后,若账号正确会进行两次302重定向,进入支付系统主页
                httpResponse = httpClient.execute(httpGet);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (autoRedirect) {
                    //判断是否成功跳转至支付平台页面
                    if (httpResponse.getStatusLine().getStatusCode() == 200
                            && document.getElementsByClass("clear main").size() > 0) {
                        return;
                    }
                } else {
                    if (httpResponse.getStatusLine().getStatusCode() == 302) {
                        httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                        httpResponse = httpClient.execute(httpGet);
                        if (httpResponse.getStatusLine().getStatusCode() == 302) {
                            httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                            httpResponse = httpClient.execute(httpGet);
                            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                            //判断是否成功跳转至支付平台页面
                            if (httpResponse.getStatusLine().getStatusCode() == 200
                                    && document.getElementsByClass("clear main").size() > 0) {
                                return;
                            }
                        }
                    }
                }
            }
        } else {
            if (autoRedirect) {
                if (httpResponse.getStatusLine().getStatusCode() == 200 && document.select("span[class='style2']").size() > 0) {
                    //开启自动重定向时的自动登录
                    httpGet = new HttpGet("http://ecard.gdei.edu.cn:8050/LoginCas.aspx");
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        httpGet = new HttpGet(document.select("a").first().attr("href"));
                        httpResponse = httpClient.execute(httpGet);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            httpGet = new HttpGet("http://ecard.gdei.edu.cn");
                            httpResponse = httpClient.execute(httpGet);
                            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                            if (document.select("div[class='right menu_a'] span em").size() > 0) {
                                return;
                            }
                        }
                    }
                }
            } else {
                if (httpResponse.getStatusLine().getStatusCode() == 302) {
                    //未开启自动重定向时的自动登录
                    httpGet = new HttpGet("https://security.gdei.edu.cn/cas/" + httpResponse.getFirstHeader("Location").getValue());
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200
                            && document.select("span[class='style2']").size() > 0) {
                        httpGet = new HttpGet("http://ecard.gdei.edu.cn");
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200
                                && document.getElementsByClass("main clear").size() > 0) {
                            if (document.select("div[class='right menu_a'] span em").size() > 0) {
                                return;
                            }
                            httpGet = new HttpGet("http://ecard.gdei.edu.cn:8050/LoginCas.aspx");
                            httpResponse = httpClient.execute(httpGet);
                            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                                httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                                httpResponse = httpClient.execute(httpGet);
                                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                    httpGet = new HttpGet(document.select("a").first().attr("href"));
                                    httpResponse = httpClient.execute(httpGet);
                                    if (httpResponse.getStatusLine().getStatusCode() == 302) {
                                        httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                                        httpResponse = httpClient.execute(httpGet);
                                        if (httpResponse.getStatusLine().getStatusCode() == 302) {
                                            httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                                            httpResponse = httpClient.execute(httpGet);
                                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                                                if (document.select("div[class='right menu_a'] span em").size() > 0) {
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        throw new ServerErrorException("支付管理平台异常");
    }

    /**
     * 发送充值请求
     *
     * @param httpClient
     * @param amount
     * @throws Exception
     */
    private Map<String, String> SendChargeRequest(CloseableHttpClient httpClient, int amount)
            throws Exception {
        HttpPost httpPost = new HttpPost("http://ecard.gdei.edu.cn/CardManage/CardInfo/DoPay");
        BasicNameValuePair basicNameValuePair1 = new BasicNameValuePair("fbankno", "epay");
        BasicNameValuePair basicNameValuePair2 = new BasicNameValuePair("tobankno", "card");
        BasicNameValuePair basicNameValuePair3 = new BasicNameValuePair("Amount", String.valueOf(amount));
        BasicNameValuePair basicNameValuePair4 = new BasicNameValuePair("password", "");
        BasicNameValuePair basicNameValuePair5 = new BasicNameValuePair("checkXieYi", "on");
        List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
        basicNameValuePairs.add(basicNameValuePair1);
        basicNameValuePairs.add(basicNameValuePair2);
        basicNameValuePairs.add(basicNameValuePair3);
        basicNameValuePairs.add(basicNameValuePair4);
        basicNameValuePairs.add(basicNameValuePair5);
        //提交表单
        httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 302) {
            HttpGet httpGet = new HttpGet("http://ecard.gdei.edu.cn/SynPay/Pay");
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //进入提交支付请求页面，获取请求参数，并模拟JS进行表单提交
                List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                Elements inputs = document.getElementsByTag("input");
                for (Element element : inputs) {
                    String name = element.attr("name");
                    String value = element.attr("value");
                    if (!name.isEmpty()) {
                        basicNameValuePairList.add(new BasicNameValuePair(name, value));
                    }
                }
                httpPost = new HttpPost("https://epay.gdei.edu.cn:8443/synpay/web/doPay");
                //绑定表单参数
                httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                httpResponse = httpClient.execute(httpPost);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //成功提交请求,检查支付平台实际预留的信息是否一致
//                    Element bd = document.getElementsByClass("bd").first();
//                    String name = bd.select("h3").first().text();
//                        if (!name.equals(chargeXm)) {
//                            //信息不一致，中止交易
//                            throw new InconsistentInformationException("用户信息不一致");
//                        }
                    httpGet = new HttpGet("https://epay.gdei.edu.cn:8443/synpay/web/disOrderInfo");
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //获取存放充值信息的DIV
                        Element main_hd = document.getElementsByClass("main_hd").first();
                        String confirmNumber = main_hd.select("span").get(0).text();
                        String confirmName = main_hd.select("span").get(1).text();
                        String confirmAmount;
                        if (amount <= 100) {
                            //小数额交易
                            confirmAmount = document.getElementsByClass("pri smallnum").first().text().substring(1);
                        } else {
                            //大数额交易
                            confirmAmount = document.getElementsByClass("pri").first().text().substring(1);
                        }
                        Map<String, String> ecardDataMap = new HashMap<>();
                        Elements ecardDatas = document.getElementsByTag("input");
                        for (Element ecardData : ecardDatas) {
                            ecardDataMap.put(ecardData.attr("name"), ecardData.attr("value"));
                        }
                        return ecardDataMap;
                    }
                    throw new ServerErrorException();
                }
                throw new ServerErrorException();
            }
            throw new ServerErrorException();
        }
        throw new ServerErrorException();
    }

    /**
     * 确认充值请求
     *
     * @param httpClient
     * @param ecardDataMap
     * @return
     * @throws Exception
     */
    private Charge ConfirmChargeRequest(CloseableHttpClient httpClient, CookieStore cookieStore
            , Map<String, String> ecardDataMap) throws Exception {
        Charge charge = new Charge();
        List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : ecardDataMap.entrySet()) {
            basicNameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        HttpPost httpPost = new HttpPost("https://epay.gdei.edu.cn:8443/synpay/web/forwardPayTool");
        //绑定表单参数
        httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            Elements inputs = document.getElementsByTag("input");
            List<BasicNameValuePair> mapiDataList = new ArrayList<>();
            for (Element input : inputs) {
                String inputName = input.attr("name");
                String inputValue = input.attr("value");
                mapiDataList.add(new BasicNameValuePair(inputName, inputValue));
            }
            httpPost = new HttpPost("https://mapi.alipay.com/gateway.do?_input_charset=utf-8");
            //绑定表单参数
            httpPost.setEntity(new UrlEncodedFormEntity(mapiDataList, StandardCharsets.UTF_8));
            //设置头信息
            httpPost.setHeader("Referer", "https://epay.gdei.edu.cn:8443/synpay/web/forwardPayTool");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
            httpPost.setHeader("Upgrade-Insecure-Requests", "1");
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                HttpGet httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 302) {
                    //获取支付宝充值接口URL地址
                    String url = httpResponse.getFirstHeader("Location").getValue();
                    httpGet = new HttpGet(url);
                    httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //获取Cookies
                        List<Cookie> cookieList = new ArrayList<>();
                        for (org.apache.http.cookie.Cookie cookie : cookieStore.getCookies()) {
                            cookieList.add(new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain()));
                        }
                        //保存支付宝充值接口URL和Cookies信息
                        charge.setCookieList(cookieList);
                        charge.setAlipayURL(url);
                        return charge;
                    }
                }
            }
        }
        throw new ServerErrorException();
    }

    /**
     * 保存用户充值记录日志
     *
     * @param sessionId
     * @param amount
     */
    @Async
    public void SaveChargeLog(String sessionId, int amount) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        ChargeLog chargeLog = new ChargeLog();
        chargeLog.setUsername(user.getUsername());
        chargeLog.setAmount(amount);
        chargeMapper.insertChargeLog(chargeLog);
    }
}
