package cn.gdeiassistant.core.charge.service;

import org.springframework.beans.factory.annotation.Value;

import cn.gdeiassistant.common.exception.chargeexception.AmountNotAvailableException;
import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.common.pojo.entity.Charge;
import cn.gdeiassistant.common.pojo.entity.Cookie;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeLogEntity;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeVO;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.utils.AnonymizeUtils;
import cn.gdeiassistant.integration.httpclient.HttpClientSession;
import cn.gdeiassistant.common.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.charge.mapper.ChargeMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.card.CardClient;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.gdeiassistant.common.i18n.Messages;

@Service
public class ChargeService {

    private final HttpClientUtils httpClientUtils;

    private final String cardBaseUrl;
    private final String paymentBaseUrl;
    private final String alipayGatewayUrl;

    private final Logger logger = LoggerFactory.getLogger(ChargeService.class);

    private final ChargeMapper chargeMapper;

    private final UserCertificateService userCertificateService;

    private final CardClient cardClient;

    public ChargeService(HttpClientUtils httpClientUtils, String casLoginUrl, String cardLoginUrl, String cardBaseUrl, String paymentBaseUrl, String alipayGatewayUrl, ChargeMapper chargeMapper, UserCertificateService userCertificateService) {
        this(httpClientUtils, cardBaseUrl, paymentBaseUrl, alipayGatewayUrl, chargeMapper, userCertificateService,
                new CardClient(httpClientUtils, cardBaseUrl, casLoginUrl, cardLoginUrl));
    }

    @Autowired
    public ChargeService(HttpClientUtils httpClientUtils, @Value("${campus.upstream.card-base-url:http://ecard.gdei.edu.cn}") String cardBaseUrl, @Value("${campus.upstream.payment-base-url:https://epay.gdei.edu.cn:8443/synpay/web}") String paymentBaseUrl, @Value("${campus.upstream.alipay-gateway-url:https://mapi.alipay.com/gateway.do?_input_charset=utf-8}") String alipayGatewayUrl, ChargeMapper chargeMapper, UserCertificateService userCertificateService, CardClient cardClient) {
        this.httpClientUtils = httpClientUtils;
        this.cardBaseUrl = cardBaseUrl;
        this.paymentBaseUrl = paymentBaseUrl;
        this.alipayGatewayUrl = alipayGatewayUrl;
        this.chargeMapper = chargeMapper;
        this.userCertificateService = userCertificateService;
        this.cardClient = cardClient;
    }

    /**
     * 提交校园卡充值请求并自动确认，返回支付宝URL和Cookie列表
     *
     * @param sessionId
     * @param amount
     * @return ChargeVO
     */
    public ChargeVO chargeRequest(String sessionId, int amount) throws Exception {
        if (amount <= 0 || amount > 500) throw new AmountNotAvailableException(Messages.get("charge.amountOutOfRange"));
        UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = httpClientUtils.getHttpClient(sessionId, false, 15);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //登录支付管理平台
            cardClient.loginCardSystem(httpClient, userCertificate.getUser().getUsername()
                    , userCertificate.getUser().getPassword(), false);
            //发送充值请求
            Map<String, String> ecardDataMap = sendChargeRequest(httpClient, amount);
            //确认充值请求
            Charge charge = confirmChargeRequest(httpClient, cookieStore, ecardDataMap);
            ChargeVO vo = new ChargeVO();
            vo.setAlipayURL(charge.getAlipayURL());
            vo.setCookieList(charge.getCookieList());
            return vo;
        } catch (AmountNotAvailableException e) {
            logger.error("校园卡充值异常：", e);
            throw new AmountNotAvailableException(Messages.get("charge.amountOutOfRange"));
        } catch (PasswordIncorrectException e) {
            logger.error("校园卡充值密码错误：", e);
            throw e;
        } catch (IOException e) {
            logger.error("校园卡充值异常：", e);
            throw new NetWorkTimeoutException(Messages.get("grade.networkTimeout"));
        } catch (Exception e) {
            logger.error("校园卡充值异常：", e);
            throw new ServerErrorException(Messages.get("charge.paymentSystemError"));
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("关闭校园卡充值 HttpClient 失败", e);
                }
            }
            if (cookieStore != null) {
                httpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 发送充值请求
     *
     * @param httpClient
     * @param amount
     * @throws Exception
     */
    private Map<String, String> sendChargeRequest(CloseableHttpClient httpClient, int amount)
            throws Exception {
        HttpPost httpPost = new HttpPost(cardBaseUrl + "/CardManage/CardInfo/DoPay");
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
            HttpGet httpGet = new HttpGet(cardBaseUrl + "/SynPay/Pay");
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
                httpPost = new HttpPost(paymentBaseUrl + "/doPay");
                //绑定表单参数
                httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                httpResponse = httpClient.execute(httpPost);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //成功提交请求,检查支付平台实际预留的信息是否一致
                    Element bd = document.getElementsByClass("bd").first();
                    String payeeName = (bd != null && bd.select("h3").first() != null)
                            ? bd.select("h3").first().text() : null;
                    httpGet = new HttpGet(paymentBaseUrl + "/disOrderInfo");
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //获取存放充值信息的DIV
                        Element main_hd = document.getElementsByClass("main_hd").first();
                        if (main_hd == null) {
                            throw new ServerErrorException(Messages.get("charge.confirmPageParseError"));
                        }
                        String confirmNumber = main_hd.select("span").get(0).text();
                        String confirmName = main_hd.select("span").get(1).text();
                        //校验两个页面返回的用户姓名是否一致，防止会话劫持导致充错账户
                        if (payeeName != null && !payeeName.isEmpty()
                                && confirmName != null && !confirmName.isEmpty()
                                && !payeeName.equals(confirmName)) {
                            logger.error("校园卡充值信息不一致: payeeName={}, confirmName={}", payeeName, confirmName);
                            throw new ServerErrorException(Messages.get("charge.userMismatchAborted"));
                        }
                        String confirmAmount;
                        if (amount <= 100) {
                            //小数额交易
                            confirmAmount = document.getElementsByClass("pri smallnum").first().text().substring(1);
                        } else {
                            //大数额交易
                            confirmAmount = document.getElementsByClass("pri").first().text().substring(1);
                        }
                        //校验确认金额与请求金额是否一致
                        try {
                            if (Integer.parseInt(confirmAmount.trim()) != amount) {
                                logger.error("校园卡充值金额不一致: requested={}, confirmed={}", amount, confirmAmount);
                                throw new ServerErrorException(Messages.get("charge.amountMismatchAborted"));
                            }
                        } catch (NumberFormatException e) {
                            logger.error("校园卡充值金额解析失败: confirmAmount={}", confirmAmount);
                            throw new ServerErrorException(Messages.get("charge.amountParseErrorAborted"));
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
    private Charge confirmChargeRequest(CloseableHttpClient httpClient, CookieStore cookieStore
            , Map<String, String> ecardDataMap) throws Exception {
        Charge charge = new Charge();
        List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : ecardDataMap.entrySet()) {
            basicNameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        HttpPost httpPost = new HttpPost(paymentBaseUrl + "/forwardPayTool");
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
            httpPost = new HttpPost(alipayGatewayUrl);
            //绑定表单参数
            httpPost.setEntity(new UrlEncodedFormEntity(mapiDataList, StandardCharsets.UTF_8));
            //设置头信息
            httpPost.setHeader("Referer", paymentBaseUrl + "/forwardPayTool");
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
                        //获取Cookies（仅保留与当前支付网关域名匹配的 Cookie，避免泄露校园网/CAS 凭证）
                        List<Cookie> cookieList = new ArrayList<>();
                        for (org.apache.http.cookie.Cookie cookie : cookieStore.getCookies()) {
                            if (matchesPaymentGatewayHost(cookie.getDomain(), url, alipayGatewayUrl)) {
                                cookieList.add(new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain()));
                            }
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
     * 保存用户充值记录日志（同步写入，确保审计记录不丢失）
     *
     * @param sessionId
     * @param amount
     */
    public void saveChargeLog(String sessionId, int amount) {
        try {
            User user = userCertificateService.getUserLoginCertificate(sessionId);
            ChargeLogEntity entity = new ChargeLogEntity();
            entity.setUsername(user.getUsername());
            entity.setAmount(amount);
            chargeMapper.insertChargeLog(entity);
        } catch (Exception e) {
            logger.error("充值审计日志写入失败: amount={}", amount, e);
        }
    }

    static boolean matchesPaymentGatewayHost(String cookieDomain, String targetUrl, String fallbackGatewayUrl) {
        if (cookieDomain == null || cookieDomain.isBlank()) {
            return false;
        }
        String targetHost = extractHost(targetUrl);
        if (targetHost == null) {
            targetHost = extractHost(fallbackGatewayUrl);
        }
        if (targetHost == null) {
            return false;
        }
        String normalizedDomain = cookieDomain.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalizedDomain.startsWith(".")) {
            normalizedDomain = normalizedDomain.substring(1);
        }
        if (normalizedDomain.isEmpty()) {
            return false;
        }
        String normalizedHost = targetHost.toLowerCase(java.util.Locale.ROOT);
        return normalizedHost.equals(normalizedDomain) || normalizedHost.endsWith("." + normalizedDomain);
    }

    private static String extractHost(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        try {
            String host = java.net.URI.create(rawUrl.trim()).getHost();
            return (host == null || host.isBlank()) ? null : host;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
