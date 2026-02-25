package cn.gdeiassistant.core.cardquery.service;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.core.cardquery.pojo.CardQuery;
import cn.gdeiassistant.core.cardquery.pojo.CardQueryResult;
import cn.gdeiassistant.common.pojo.Entity.Card;
import cn.gdeiassistant.common.pojo.Entity.CardInfo;
import cn.gdeiassistant.integration.card.CardClient;
import cn.gdeiassistant.integration.httpclient.HttpClientSession;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.core.imageRecognition.service.ImageRecognitionService;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import cn.gdeiassistant.common.tools.Utils.ImageEncodeUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CardQueryService {

    private final Logger logger = LoggerFactory.getLogger(CardQueryService.class);

    @Autowired
    private ImageRecognitionService imageRecognitionService;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private CardClient cardClient;

    /**
     * 查询饭卡基本信息
     */
    public CardInfo cardInfoQuery(String sessionId) throws Exception {
        UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, 15);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            LoginCardSystem(httpClient, userCertificate.getUser().getUsername()
                    , userCertificate.getUser().getPassword(), true);
            HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            Document document = cardClient.fetchCardBasicInfoDocument(sessionId);
            return parseCardInfoFromDocument(document);
        } catch (PasswordIncorrectException ignored) {
            throw new PasswordIncorrectException("账户密码不正确");
        } catch (ServerErrorException e) {
            logger.error("查询饭卡基本信息异常：", e);
            throw new ServerErrorException("支付管理系统异常");
        } catch (IOException e) {
            logger.error("查询饭卡基本信息异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("查询饭卡基本信息异常", e);
            throw new ServerErrorException("支付管理系统异常");
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("关闭饭卡基本信息查询 HttpClient 失败", e);
                }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 查询消费流水
     */
    public CardQueryResult cardQuery(String sessionId, CardQuery cardQuery) throws Exception {
        UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, 15);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            LoginCardSystem(httpClient, userCertificate.getUser().getUsername(),
                    userCertificate.getUser().getPassword(), true);
            HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            CardInfo cardInfo = parseCardInfoFromDocument(cardClient.fetchCardBasicInfoDocument(sessionId));
            List<Card> cardList = queryCardListViaClient(sessionId, cardQuery);
            CardQueryResult cardQueryResult = new CardQueryResult();
            cardQueryResult.setCardInfo(cardInfo);
            cardQueryResult.setCardList(cardList);
            cardQueryResult.setCardQuery(cardQuery);
            return cardQueryResult;
        } catch (PasswordIncorrectException ignored) {
            throw new PasswordIncorrectException("账户密码不正确");
        } catch (IOException e) {
            logger.error("查询消费流水异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("查询消费流水异常：", e);
            throw new ServerErrorException("支付管理系统异常");
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("关闭消费流水查询 HttpClient 失败", e);
                }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 校园卡挂失
     */
    public void cardLost(String sessionId, String cardPassword) throws Exception {
        UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, 15);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            LoginCardSystem(httpClient, userCertificate.getUser().getUsername()
                    , userCertificate.getUser().getPassword(), true);
            HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            submitCardLostViaClient(sessionId, cardPassword);
        } catch (PasswordIncorrectException ignored) {
            throw new PasswordIncorrectException("账户密码不正确");
        } catch (IOException e) {
            logger.error("校园卡挂失异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("校园卡挂失异常：", e);
            throw new ServerErrorException("支付管理系统异常");
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("关闭校园卡挂失 HttpClient 失败", e);
                }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 登录支付管理平台（CAS + ecard，严禁修改 CAS 相关逻辑）
     */
    private void LoginCardSystem(CloseableHttpClient httpClient, String username, String password, boolean autoRedirect) throws IOException, ServerErrorException, PasswordIncorrectException {
        HttpGet httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.getElementsByClass("pcclient").size() > 0) {
            List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
            basicNameValuePairs.add(new BasicNameValuePair("imageField.x", "0"));
            basicNameValuePairs.add(new BasicNameValuePair("imageField.y", "0"));
            basicNameValuePairs.add(new BasicNameValuePair("username", username));
            basicNameValuePairs.add(new BasicNameValuePair("password", password));
            basicNameValuePairs.add(new BasicNameValuePair("service", "http://ecard.gdei.edu.cn:8050/LoginCas.aspx"));
            basicNameValuePairs.add(new BasicNameValuePair("tokens", document.getElementById("tokens").val()));
            basicNameValuePairs.add(new BasicNameValuePair("stamp", document.getElementById("stamp").val()));
            HttpPost httpPost = new HttpPost("https://security.gdei.edu.cn/cas/login");
            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
            httpResponse = httpClient.execute(httpPost);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                if (document.getElementsByClass("pcclient").size() > 0) {
                    throw new PasswordIncorrectException("用户名或密码错误");
                }
                httpGet = new HttpGet(document.select("a").first().attr("href"));
                httpResponse = httpClient.execute(httpGet);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (autoRedirect) {
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

    private static CardInfo parseCardInfoFromDocument(Document document) {
        String name = document.getElementsByClass("Jbinfo").first().select("em").first().text();
        String number = document.getElementsByClass("Jbinfo").first().select("em").get(1).text();
        String cardNumber = document.getElementsByClass("Jbinfo").get(1).select("em").get(0).text().replace(" ", "");
        String cardBalance = document.getElementsByClass("Jbinfo").get(1).select("em").get(1).text();
        String cardInterimBalance = document.getElementsByClass("Jbinfo").get(2).select("em").text();
        String cardLostState = document.getElementsByClass("careful").get(0).select("em").get(0).text().replace(" ", "");
        String cardFreezeState = document.getElementsByClass("careful").get(0).select("em").get(1).text();
        CardInfo cardInfo = new CardInfo();
        cardInfo.setName(name);
        cardInfo.setNumber(number);
        cardInfo.setCardNumber(cardNumber);
        cardInfo.setCardBalance(cardBalance);
        cardInfo.setCardInterimBalance(cardInterimBalance);
        cardInfo.setCardLostState(cardLostState);
        cardInfo.setCardFreezeState(cardFreezeState);
        return cardInfo;
    }

    private List<Card> queryCardListViaClient(String sessionId, CardQuery cardQuery) throws IOException, ServerErrorException {
        Calendar currentTime = Calendar.getInstance();
        boolean isToday = false;
        if (currentTime.get(Calendar.YEAR) == cardQuery.getYear()) {
            if (currentTime.get(Calendar.MONTH) == cardQuery.getMonth() - 1) {
                if (currentTime.get(Calendar.DAY_OF_MONTH) == cardQuery.getDate()) {
                    isToday = true;
                }
            }
        }
        if (isToday) {
            Document document = cardClient.fetchCardTrjnListDocument(sessionId, 0, 1);
            List<Card> cardList = new ArrayList<>();
            Element table = document.getElementsByClass("table_show").get(0);
            Elements trs = table.select("tr");
            if (trs.get(1).select("td").text().equals("当前查询条件内没有流水记录")) {
                return cardList;
            }
            String[] strings = trs.get(1).select("td").text().split(" ");
            int listsCount = Integer.valueOf(strings[2].substring(6));
            int pagesCount = listsCount % 10 != 0 ? listsCount / 10 + 1 : listsCount / 10;
            for (int i = 1; i <= pagesCount; i++) {
                Document pageDoc = i == 1 ? document : cardClient.fetchCardTrjnListDocument(sessionId, 0, i);
                table = pageDoc.getElementsByClass("table_show").get(0);
                trs = table.select("tr");
                for (int j = 2; j < trs.size(); j++) {
                    String tradeTime = trs.get(j).select("td").get(0).text();
                    String merchantName = trs.get(j).select("td").get(1).text();
                    String tradeName = trs.get(j).select("td").get(3).text();
                    String tradePrice = trs.get(j).select("td").get(4).text();
                    String accountBalance = trs.get(j).select("td").get(5).text();
                    Card card = new Card();
                    card.setTradeTime(tradeTime);
                    card.setMerchantName(merchantName);
                    card.setTradeName(tradeName);
                    card.setTradePrice(tradePrice);
                    card.setAccountBalance(accountBalance);
                    cardList.add(card);
                }
            }
            return cardList;
        } else {
            Document document = cardClient.fetchCardTrjnListByDateDocument(sessionId, cardQuery.getYear(), cardQuery.getMonth(), cardQuery.getDate(), 1);
            List<Card> cardList = new ArrayList<>();
            Element table = document.getElementsByClass("table_show").get(0);
            Elements trs = table.select("tr");
            if (trs.get(1).select("td").text().equals("当前查询条件内没有流水记录")) {
                return cardList;
            }
            String[] strings = trs.get(1).select("td").text().split(" ");
            int listsCount = Integer.valueOf(strings[4].substring(6));
            int pagesCount = listsCount % 10 != 0 ? listsCount / 10 + 1 : listsCount / 10;
            for (int i = 1; i <= pagesCount; i++) {
                Document pageDoc = i == 1 ? document : cardClient.fetchCardTrjnListByDateDocument(sessionId, cardQuery.getYear(), cardQuery.getMonth(), cardQuery.getDate(), i);
                table = pageDoc.getElementsByClass("table_show").get(0);
                trs = table.select("tr");
                for (int j = 2; j < trs.size(); j++) {
                    String tradeTime = trs.get(j).select("td").get(0).text();
                    String merchantName = trs.get(j).select("td").get(1).text();
                    String tradeName = trs.get(j).select("td").get(3).text();
                    String tradePrice = trs.get(j).select("td").get(4).text();
                    String accountBalance = trs.get(j).select("td").get(5).text();
                    Card card = new Card();
                    card.setTradeTime(tradeTime);
                    card.setMerchantName(merchantName);
                    card.setTradeName(tradeName);
                    card.setTradePrice(tradePrice);
                    card.setAccountBalance(accountBalance);
                    cardList.add(card);
                }
            }
            return cardList;
        }
    }

    private void submitCardLostViaClient(String sessionId, String cardPassword) throws ServerErrorException, RecognitionException, IOException, PasswordIncorrectException {
        Document document = cardClient.fetchLossCardPageDocument(sessionId);
        int i = 1;
        int j = 1;
        while (i <= 3) {
            byte[] keyPadBytes = cardClient.fetchKeyPadImage(sessionId);
            String safeKeyBoard = imageRecognitionService.CharacterNumberRecognize(ImageEncodeUtils.convertToBase64(new ByteArrayInputStream(keyPadBytes)));
            if (safeKeyBoard.length() != 10 || !safeKeyBoard.matches("^[0-9]*$")) {
                i++;
                continue;
            }
            while (j <= 3) {
                String checkcodePath = document.getElementById("imgCheckCode").attr("src");
                byte[] checkcodeBytes = cardClient.fetchCheckcodeImage(sessionId, checkcodePath);
                String checkCode = imageRecognitionService.CheckCodeRecognize(ImageEncodeUtils.convertToBase64(new ByteArrayInputStream(checkcodeBytes), ImageEncodeUtils.ImageFormTypeEnum.PNG), CheckCodeTypeEnum.NUMBER, 4);
                if (checkCode.length() != 4 || !checkCode.matches("^[0-9]*$")) {
                    j++;
                    continue;
                }
                Map<Integer, Integer> safeKeyBoardMap = new HashMap<>();
                for (int m = 0; m < 10; m++) {
                    safeKeyBoardMap.put(Integer.valueOf(String.valueOf(safeKeyBoard.charAt(m))), m);
                }
                StringBuilder builder = new StringBuilder();
                for (int n = 0; n < cardPassword.length(); n++) {
                    builder.append(safeKeyBoardMap.get(Integer.valueOf(String.valueOf(cardPassword.charAt(n)))));
                }
                String password = builder.reverse().toString();
                String jsonStr = cardClient.submitSetCardLost(sessionId, password, checkCode);
                JSONObject jsonObject = JSON.parseObject(jsonStr);
                if (jsonObject.containsKey("ret") && jsonObject.containsKey("msg")) {
                    if (jsonObject.getBoolean("ret")) {
                        return;
                    }
                    String message = jsonObject.getString("msg");
                    if ("验证码不正确".equals(message)) {
                        throw new RecognitionException("识别验证码图片失败");
                    } else if ("查询密码错误".equals(message)) {
                        throw new PasswordIncorrectException("校园卡密码错误");
                    }
                    throw new ServerErrorException("支付管理平台系统异常");
                }
                throw new ServerErrorException("支付管理平台系统异常");
            }
            throw new RecognitionException("识别验证码图片失败");
        }
        throw new RecognitionException("识别安全键盘图片失败");
    }
}
