package com.linguancheng.gdeiassistant.Service.ChargeQuery;

import com.linguancheng.gdeiassistant.Cookie.HttpClientCookieManager;
import com.linguancheng.gdeiassistant.Enum.Charge.ChargeRequestResultEnum;
import com.linguancheng.gdeiassistant.Enum.Charge.GetServerKeyCodeResultEnum;
import com.linguancheng.gdeiassistant.Enum.Charge.VerifyClientKeyCodeResultEnum;
import com.linguancheng.gdeiassistant.Exception.ChargeException.*;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistantLogs.Charge.ChargeMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.CardInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.Charge;
import com.linguancheng.gdeiassistant.Pojo.Entity.ChargeLog;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.taobao.wsgsvr.WsgException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpSessionRequiredException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ChargeService {

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Autowired
    private ChargeMapper chargeMapper;

    @Autowired
    private HttpClientCookieManager httpClientCookieManager;

    private Log log = LogFactory.getLog(ChargeService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.charge']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 校验客户端真实性
     *
     * @param clientKeycode
     * @param timeStamp
     * @return
     */
    public VerifyClientKeyCodeResultEnum VerifyClientKeyCode(String clientKeycode, String timeStamp) {
        String realKeycode = "GdeiAssistant" + timeStamp;
        try {
            if (clientKeycode.equals(StringEncryptUtils.SHA1HexString(StringEncryptUtils.encryptString(realKeycode)))) {
                return VerifyClientKeyCodeResultEnum.VERIFY_SUCCESS;
            } else {
                return VerifyClientKeyCodeResultEnum.VERIFY_FAILURE;
            }
        } catch (WsgException e) {
            log.error("校园卡充值校验客户端异常：", e);
            return VerifyClientKeyCodeResultEnum.VERIFY_EXCEPTION;
        }
    }

    /**
     * 保存用户充值记录日志
     *
     * @param username
     * @param amount
     */
    public void SaveChargeLog(String username, Integer amount) {
        try {
            ChargeLog chargeLog = new ChargeLog();
            chargeLog.setUsername(username);
            chargeLog.setAmount(amount);
            chargeLog.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            chargeMapper.insertChargeLog(chargeLog);
        } catch (Exception e) {
            log.error("保存用户校园卡充值记录异常：", e);
        }
    }

    /**
     * 返回供客户端校验的服务端Keycode
     *
     * @param user
     * @param securityVersion
     * @param clientType
     * @param timeStamp
     * @param amount
     * @return
     */
    public BaseResult<String, GetServerKeyCodeResultEnum> GetServerKeycode(User user, String securityVersion, String clientType, String timeStamp, int amount) {
        BaseResult<String, GetServerKeyCodeResultEnum> result = new BaseResult<>();
        try {
            if (clientType.equals("Android")) {
                String androidServerKeycode = GetAndroidServerKeycode(user, securityVersion, timeStamp, amount);
                result.setResultType(GetServerKeyCodeResultEnum.GETKEYCODE_SUCCESS);
                result.setResultData(androidServerKeycode);
                return result;
            }
            throw new UnsupportClientTypeException();
        } catch (UnsupportSecurityVersionException e) {
            log.error("校园卡充值生成服务端校验码异常：", e);
            result.setResultType(GetServerKeyCodeResultEnum.UNSUPPORT_SECURITYVERSION);
        } catch (UnsupportClientTypeException e) {
            log.error("校园卡充值生成服务端校验码异常：", e);
            result.setResultType(GetServerKeyCodeResultEnum.UNSUPPORT_CLIENTTYPE);
        } catch (WsgException e) {
            log.error("校园卡充值生成服务端校验码异常：", e);
            result.setResultType(GetServerKeyCodeResultEnum.INCORRECT_USERINFORMATION);
        }
        return result;
    }

    /**
     * 返回供安卓客户端校验的服务端Keycode
     *
     * @param user
     * @param securityVersion
     * @param timeStamp
     * @param amount
     * @return
     */
    private String GetAndroidServerKeycode(User user, String securityVersion, String timeStamp, int amount) throws UnsupportSecurityVersionException, WsgException {
        if (securityVersion.equals("1.1")) {
            //1.1版本的安全校验
            String text = user.getUsername() + user.getPassword() + amount + "GdeiAssistant" + timeStamp;
            return StringEncryptUtils.SHA1HexString(StringEncryptUtils.encryptString(text));
        }
        throw new UnsupportSecurityVersionException();
    }

    /**
     * 提交校园卡充值请求并自动确认,返回支付宝URL和Cookie列表
     *
     * @param request
     * @param amount
     * @return
     */
    public BaseResult<Charge, ChargeRequestResultEnum> ChargeRequest(HttpServletRequest request, String username, String password, int amount) {
        BaseResult<Charge, ChargeRequestResultEnum> result = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        try {
            if (amount <= 0 || amount > 500) {
                throw new AccountNotAvailableException("充值金额超过范围");
            }
            httpClient = httpClientFactory.getHttpClient(request.getSession(), true, timeout);
            //登录支付管理平台
            CardInfo cardInfo = LoginCardSystem(httpClient, username, password);
            //发送充值请求
            Map<String, String> ecardDataMap = SendChargeRequest(httpClient, cardInfo.getName(), amount);
            //确认充值请求
            Charge charge = ConfirmChargeRequest(httpClient, request, ecardDataMap);
            result.setResultData(charge);
            result.setResultType(ChargeRequestResultEnum.REQUEST_SUCCESS);
            return result;
        } catch (AccountNotAvailableException e) {
            log.error("校园卡充值异常：", e);
            result.setResultType(ChargeRequestResultEnum.ACCOUNT_NOT_AVAILABLE);
        } catch (InconsistentInformationException e) {
            log.error("校园卡充值异常：", e);
            result.setResultType(ChargeRequestResultEnum.INCONSISTENT_INFORMATION);
        } catch (Exception e) {
            log.error("校园卡充值异常：", e);
            result.setResultType(ChargeRequestResultEnum.SERVER_ERROR);
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
     * 登录支付管理平台
     *
     * @param httpClient
     * @param username
     * @param password
     * @throws IOException
     * @throws ServerErrorException
     * @throws PasswordIncorrectException
     */
    private CardInfo LoginCardSystem(CloseableHttpClient httpClient, String username, String password) throws
            IOException, ServerErrorException, PasswordIncorrectException {
        HttpGet httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login?service=http://ecard.gdei.edu.cn:8050/LoginCas.aspx");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("广东第二师范学院中央认证服务－登录")) {
            //封装需要提交的数据
            BasicNameValuePair basicNameValuePair_1 = new BasicNameValuePair("imageField.x", "0");
            BasicNameValuePair basicNameValuePair_2 = new BasicNameValuePair("imageField.y", "0");
            BasicNameValuePair basicNameValuePair_3 = new BasicNameValuePair("username", username);
            BasicNameValuePair basicNameValuePair_4 = new BasicNameValuePair("password", password);
            BasicNameValuePair basicNameValuePair_5 = new BasicNameValuePair("service", "http%3A%2F%2Fecard.gdei.edu.cn%3A8050%2FLoginCas.aspx");
            List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
            basicNameValuePairs.add(basicNameValuePair_1);
            basicNameValuePairs.add(basicNameValuePair_2);
            basicNameValuePairs.add(basicNameValuePair_3);
            basicNameValuePairs.add(basicNameValuePair_4);
            basicNameValuePairs.add(basicNameValuePair_5);
            HttpPost httpPost = new HttpPost("https://security.gdei.edu.cn/cas/login?service=http://ecard.gdei.edu.cn:8050/LoginCas.aspx");
            //绑定表单参数
            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
            httpResponse = httpClient.execute(httpPost);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                if (document.title().equals("广东第二师范学院中央认证服务－登录")) {
                    throw new PasswordIncorrectException("用户名或密码错误");
                }
                //获取html页面中的首个URL地址,进入支付系统页面
                httpGet = new HttpGet(document.select("a").first().attr("href"));
                //请求后,若账号正确会进行两次302重定向,进入支付系统主页
                httpResponse = httpClient.execute(httpGet);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                //通过标题判断是否成功跳转至支付平台页面
                if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("广东第二师范学院支付平台")) {
                    httpGet = new HttpGet("http://ecard.gdei.edu.cn/CardManage/CardInfo/Transfer");
                    httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        Element first_Jbinfo = document.getElementsByClass("Jbinfo").first();
                        Element second_Jbinfo = document.getElementsByClass("Jbinfo").get(1);
                        String chargedName = first_Jbinfo.select("em").first().text();
                        String chargeNumber = first_Jbinfo.select("em").get(1).text();
                        String chargeBalance = second_Jbinfo.select("em").first().text();
                        CardInfo cardInfo = new CardInfo();
                        //缓存校园卡基本信息
                        cardInfo.setName(chargedName);
                        cardInfo.setNumber(chargeNumber);
                        cardInfo.setCardBalance(chargeBalance);
                        //登录成功
                        return cardInfo;
                    }
                    throw new ServerErrorException("支付管理平台异常");
                }
                throw new ServerErrorException("支付管理平台异常");
            }
            throw new ServerErrorException("支付管理平台异常");
        } else if (httpResponse.getStatusLine().getStatusCode() == 200) {
            //自动登录
            httpGet = new HttpGet(document.select("a").first().attr("href"));
            //请求后,若账号正确会进行两次302重定向,进入支付系统主页
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            //通过标题判断是否成功跳转至支付平台页面
            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("广东第二师范学院支付平台")) {
                httpGet = new HttpGet("http://ecard.gdei.edu.cn/CardManage/CardInfo/Transfer");
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    Element first_Jbinfo = document.getElementsByClass("Jbinfo").first();
                    Element second_Jbinfo = document.getElementsByClass("Jbinfo").get(1);
                    String chargedName = first_Jbinfo.select("em").first().text();
                    String chargeNumber = first_Jbinfo.select("em").get(1).text();
                    String chargeBalance = second_Jbinfo.select("em").first().text();
                    CardInfo cardInfo = new CardInfo();
                    //缓存校园卡基本信息
                    cardInfo.setName(chargedName);
                    cardInfo.setNumber(chargeNumber);
                    cardInfo.setCardBalance(chargeBalance);
                    //登录成功
                    return cardInfo;
                }
                throw new ServerErrorException("支付管理平台异常");
            }
            throw new ServerErrorException("支付管理平台异常");
        }
        throw new ServerErrorException("支付管理平台异常");
    }

    /**
     * 发送充值请求
     *
     * @param httpClient
     * @param chargeXm
     * @param amount
     * @throws AccountNotAvailableException
     * @throws IOException
     * @throws RequestExpiredException
     * @throws InconsistentInformationException
     * @throws ServerErrorException
     */
    private Map<String, String> SendChargeRequest(CloseableHttpClient httpClient, String chargeXm, int amount) throws IOException, RequestExpiredException, InconsistentInformationException, ServerErrorException {
        HttpPost httpPost = new HttpPost("http://ecard.gdei.edu.cn/CardManage/CardInfo/DoPay");
        BasicNameValuePair basicNameValuePair1 = new BasicNameValuePair("fbankno", "epay");
        BasicNameValuePair basicNameValuePair2 = new BasicNameValuePair("tobankno", "card");
        BasicNameValuePair basicNameValuePair3 = new BasicNameValuePair("Amount", amount + "");
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
        if (httpResponse.getStatusLine().getStatusCode() == 200 && (document.title().equals("Error")) || document.title().equals("广东第二师范学院支付平台")) {
            //身份凭证过期，重新连接
            throw new RequestExpiredException("身份凭证过期");
        } else {
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                HttpGet httpGet = new HttpGet("http://ecard.gdei.edu.cn/SynPay/Pay");
                httpResponse = httpClient.execute(httpGet);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("服务平台-提交支付请求")) {
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
                    if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("广东第二师范学院支付平台")) {
                        //成功提交请求,检查支付平台实际预留的信息是否一致
                        Element bd = document.getElementsByClass("bd").first();
                        String name = bd.select("h3").first().text();
//                        if (!name.equals(chargeXm)) {
//                            //信息不一致，中止交易
//                            throw new InconsistentInformationException("用户信息不一致");
//                        }
                        httpGet = new HttpGet("https://epay.gdei.edu.cn:8443/synpay/web/disOrderInfo");
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("广东第二师范学院支付平台")) {
                            //获取存放充值信息的DIV
                            Element main_hd = document.getElementsByClass("main_hd").first();
                            String confirmNumber = main_hd.select("span").get(0).text();
                            String confirmName = main_hd.select("span").get(1).text();
                            String confirmAmount;
                            if (Integer.valueOf(amount) <= 100) {
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
                        } else {
                            throw new ServerErrorException();
                        }
                    } else {
                        throw new ServerErrorException();
                    }
                } else {
                    throw new ServerErrorException();
                }
            } else {
                throw new ServerErrorException();
            }
        }
    }

    /**
     * 确认充值请求
     *
     * @param httpClient
     * @param request
     * @return
     * @throws IOException
     * @throws ServerErrorException
     * @throws RequestExpiredException
     */

    private Charge ConfirmChargeRequest(CloseableHttpClient httpClient, HttpServletRequest request, Map<String, String> ecardDataMap) throws IOException, ServerErrorException, RequestExpiredException, HttpSessionRequiredException {
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
        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("广东第二师范学院支付平台")) {
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
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                HttpGet httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //获取订单支付URL
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    String J_orderId = document.getElementById("J_orderId").attr("value");
                    if (J_orderId != null && !J_orderId.isEmpty()) {
                        //提交确认充值请求成功
                        String url = "https://excashier.alipay.com/standard/auth.htm?payOrderId=" + J_orderId;
                        httpGet = new HttpGet(url);
                        httpClient.execute(httpGet);
                        //获取Cookies
                        List<Cookie> cookieList = httpClientCookieManager.getCookieList(request);
                        //保存支付宝充值接口URL和Cookies信息
                        charge.setCookieList(cookieList);
                        charge.setAlipayURL(url);
                        return charge;
                    } else {
                        throw new ServerErrorException();
                    }
                } else {
                    throw new ServerErrorException();
                }
            } else {
                throw new ServerErrorException();
            }
        } else if (httpResponse.getStatusLine().getStatusCode() == 200) {
            //会话超时关闭
            throw new RequestExpiredException("会话超时关闭");
        } else {
            throw new ServerErrorException();
        }
    }
}
