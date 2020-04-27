package edu.gdei.gdeiassistant.Service.CardQuery;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import edu.gdei.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.RecognitionException.RecognitionException;
import edu.gdei.gdeiassistant.Pojo.CardQuery.CardQuery;
import edu.gdei.gdeiassistant.Pojo.CardQuery.CardQueryResult;
import edu.gdei.gdeiassistant.Pojo.Entity.Card;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import edu.gdei.gdeiassistant.Service.Recognition.RecognitionService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.ImageEncodeUtils;
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
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CardQueryService {

    @Autowired
    private RecognitionService recognitionService;

    private Logger logger = LoggerFactory.getLogger(CardQueryService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.cardquery']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 查询饭卡基本信息
     *
     * @param sessionId
     * @param username
     * @param password
     * @return
     */
    public CardInfo CardInfoQuery(String sessionId, String username, String password) throws Exception {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //登录支付管理平台
            LoginCardSystem(httpClient, username, password, true);
            //获取饭卡基本信息
            return QueryCardInformation(httpClient);
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
                    e.printStackTrace();
                }
            }
            if (cookieStore != null) {
                HttpClientUtils.SyncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 查询消费流水
     *
     * @param sessionId
     * @param cardQuery
     * @return
     */
    public CardQueryResult CardQuery(String sessionId, String username, String password, CardQuery cardQuery) throws Exception {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //登录支付管理平台
            LoginCardSystem(httpClient, username, password, true);
            //获取饭卡基本信息
            CardInfo cardInfo = QueryCardInformation(httpClient);
            //获取消费记录流水
            List<Card> cardList = QueryCardList(httpClient, cardQuery);
            //查询成功
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
                    e.printStackTrace();
                }
            }
            if (cookieStore != null) {
                HttpClientUtils.SyncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 校园卡挂失
     *
     * @param sessionId
     * @param username
     * @param password
     * @param cardPassword
     * @return
     */
    public void CardLost(String sessionId, String username, String password, String cardPassword) throws Exception {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //登录支付管理平台
            LoginCardSystem(httpClient, username, password, true);
            //提交校园卡挂失请求
            SubmitCardLostRequest(httpClient, cardPassword);
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
     * @throws IOException
     * @throws ServerErrorException
     * @throws PasswordIncorrectException
     */
    public void LoginCardSystem(CloseableHttpClient httpClient, String username, String password, boolean autoRedirect) throws IOException, ServerErrorException, PasswordIncorrectException {
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
     * 成功登录支付管理平台后调用此方法进行查询校园卡基本信息
     *
     * @param httpClient
     * @return
     * @throws ServerErrorException
     * @throws IOException
     */
    public CardInfo QueryCardInformation(CloseableHttpClient httpClient) throws ServerErrorException, IOException {
        //获取校园卡基本信息
        HttpGet httpGet = new HttpGet("http://ecard.gdei.edu.cn/CardManage/CardInfo/BasicInfo");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            //获取基本信息
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
        } else {
            throw new ServerErrorException("支付管理平台系统异常");
        }
    }

    /**
     * 成功登录支付管理平台后调用此方法挂失校园卡
     *
     * @param httpClient
     * @return
     * @throws ServerErrorException
     * @throws RecognitionException
     * @throws IOException
     */
    private void SubmitCardLostRequest(CloseableHttpClient httpClient, String cardPassword) throws ServerErrorException
            , RecognitionException, IOException, PasswordIncorrectException {
        HttpPost httpPost = new HttpPost("http://ecard.gdei.edu.cn/CardManage/CardInfo/LossCard");
        List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
        basicNameValuePairList.add(new BasicNameValuePair("needHeader", "false"));
        httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            //解析安全键盘图片的次数，超过3次则直接抛出异常结束重试
            int i = 1;
            //解析验证码图片的次数，超过3次则直接抛出异常结束重试
            int j = 1;
            while (i <= 3) {
                //加载安全键盘图片
                HttpGet httpGet = new HttpGet("http://ecard.gdei.edu.cn/Account/GetNumKeyPadImg");
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    InputStream safeKeyBoardImage = httpResponse.getEntity().getContent();
                    //将安全键盘图片输入流转换为base64编码
                    String safeKeyBoard = recognitionService.CharacterNumberRecognize(ImageEncodeUtils.ConvertToBase64(safeKeyBoardImage));
                    //验证安全键盘识别文字是否合法
                    if (safeKeyBoard.length() != 10 || !safeKeyBoard.matches("^[0-9]*$")) {
                        i++;
                        continue;
                    }
                    while (j <= 3) {
                        //加载验证码图片
                        httpGet = new HttpGet("http://ecard.gdei.edu.cn" + document.getElementById("imgCheckCode").attr("src"));
                        httpResponse = httpClient.execute(httpGet);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            InputStream checkCodeImage = httpResponse.getEntity().getContent();
                            //将验证码图片输入流转换为base64编码
                            String checkCode = recognitionService.CheckCodeRecognize(ImageEncodeUtils
                                            .ConvertToBase64(checkCodeImage, ImageEncodeUtils.ImageFormTypeEnum.PNG)
                                    , CheckCodeTypeEnum.NUMBER, 4);
                            if (checkCode.length() != 4 || !checkCode.matches("^[0-9]*$")) {
                                j++;
                                continue;
                            }
                            //设置真实密码值与图片位置值的映射
                            Map<Integer, Integer> safeKeyBoardMap = new HashMap<>();
                            for (int m = 0; m < 10; m++) {
                                safeKeyBoardMap.put(Integer.valueOf(String.valueOf(safeKeyBoard.charAt(m))), m);
                            }
                            StringBuilder builder = new StringBuilder();
                            for (int n = 0; n < cardPassword.length(); n++) {
                                builder.append(safeKeyBoardMap.get(Integer.valueOf(String.valueOf(cardPassword.charAt(n)))));
                            }
                            String password = builder.reverse().toString();
                            //提交挂失请求
                            httpPost = new HttpPost("http://ecard.gdei.edu.cn/CardManage/CardInfo/SetCardLost");
                            List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
                            basicNameValuePairs.add(new BasicNameValuePair("password", password));
                            basicNameValuePairs.add(new BasicNameValuePair("checkCode", checkCode));
                            //绑定请求参数
                            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
                            //设置请求头信息
                            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
                            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
                            httpPost.setHeader("Referer", "http://ecard.gdei.edu.cn/");
                            httpResponse = httpClient.execute(httpPost);
                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                JSONObject jsonObject = JSON.parseObject(EntityUtils.toString(httpResponse.getEntity()));
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
                            throw new ServerErrorException("支付管理平台系统异常");
                        }
                        throw new ServerErrorException("支付管理平台系统异常");
                    }
                    throw new RecognitionException("识别验证码图片失败");
                }
                throw new ServerErrorException("支付管理平台系统异常");
            }
            throw new RecognitionException("识别安全键盘图片失败");
        }
        throw new ServerErrorException("支付管理平台异常");
    }

    /**
     * 成功登录支付管理平台后调用此方法进行查询消费记录流水
     *
     * @param httpClient
     * @param cardQuery
     * @return
     * @throws IOException
     * @throws ServerErrorException
     */
    private List<Card> QueryCardList(CloseableHttpClient httpClient, CardQuery cardQuery) throws IOException, ServerErrorException {
        //判断查询日期是否为今天
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
            //查询当日消费记录
            HttpGet httpGet = new HttpGet("http://ecard.gdei.edu.cn/CardManage/CardInfo/TrjnList?type=0");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //交易流水信息列表
                List<Card> cardList = new ArrayList<>();
                //获取存放流水记录的表格
                Element table = document.getElementsByClass("table_show").get(0);
                //获取表格内所有的行
                Elements trs = table.select("tr");
                //首行为列信息,第二行记录总记录数或提示当前查询条件内没有流水记录
                if (trs.get(1).select("td").text().equals("当前查询条件内没有流水记录")) {
                    //无流水记录
                    return cardList;
                } else {
                    //获取记录总数,用于判断页数
                    String[] strings = trs.get(1).select("td").text().split(" ");
                    int listsCount = Integer.valueOf(strings[2].substring(6));
                    int pagesCount;
                    if (listsCount % 10 != 0) {
                        pagesCount = listsCount / 10 + 1;
                    } else {
                        pagesCount = listsCount / 10;
                    }
                    //从第三行开始获取流水记录
                    for (int i = 1; i <= pagesCount; i++) {
                        //逐页获取流水信息
                        httpGet = new HttpGet("http://ecard.gdei.edu.cn/CardManage/CardInfo/TrjnList?type=0&pageindex=" + i);
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            //获取存放流水记录的表格
                            table = document.getElementsByClass("table_show").get(0);
                            //获取表格内所有的行
                            trs = table.select("tr");
                            //从第三行开始获取流水记录
                            for (int j = 2; j < trs.size(); j++) {
                                //获取流水记录并添加到列表中
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
                    }
                    return cardList;
                }
            } else {
                throw new ServerErrorException("支付管理平台系统异常");
            }
        } else {
            //查询历史消费记录
            String url = "http://ecard.gdei.edu.cn/CardManage/CardInfo/TrjnList?beginTime=" + cardQuery.getYear() + "-" + cardQuery.getMonth() + "-" + cardQuery.getDate() + "&endTime=" + cardQuery.getYear() + "-" + cardQuery.getMonth() + "-" + cardQuery.getDate() + "&type=1";
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //交易流水信息列表
                List<Card> cardList = new ArrayList<>();
                //获取存放流水记录的表格
                Element table = document.getElementsByClass("table_show").get(0);
                //获取表格内所有的行
                Elements trs = table.select("tr");
                //首行为列信息,第二行记录总记录数或提示当前查询条件内没有流水记录
                if (trs.get(1).select("td").text().equals("当前查询条件内没有流水记录")) {
                    //无流水记录
                    return cardList;
                } else {
                    //获取记录总数,用于判断页数
                    String[] strings = trs.get(1).select("td").text().split(" ");
                    int listsCount = Integer.valueOf(strings[4].substring(6));
                    int pagesCount;
                    if (listsCount % 10 != 0) {
                        pagesCount = listsCount / 10 + 1;
                    } else {
                        pagesCount = listsCount / 10;
                    }
                    //从第三行开始获取流水记录
                    for (int i = 1; i <= pagesCount; i++) {
                        //逐页获取流水信息
                        httpGet = new HttpGet("http://ecard.gdei.edu.cn/CardManage/CardInfo/TrjnList?beginTime=" + cardQuery.getYear() + "-" + cardQuery.getMonth() + "-" + cardQuery.getDate() + "&endTime=" + cardQuery.getYear() + "-" + cardQuery.getMonth() + "-" + cardQuery.getDate() + "&type=1&pageindex=" + i);
                        httpResponse = httpClient.execute(httpGet);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                            //获取存放流水记录的表格
                            table = document.getElementsByClass("table_show").get(0);
                            //获取表格内所有的行
                            trs = table.select("tr");
                            //从第三行开始获取流水记录
                            for (int j = 2; j < trs.size(); j++) {
                                //获取流水记录并添加到列表中
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
                        } else {
                            throw new ServerErrorException("支付管理平台系统异常");
                        }
                    }
                    //获取所有流水信息成功
                    return cardList;
                }
            } else {
                throw new ServerErrorException("支付管理平台系统异常");
            }
        }
    }
}
