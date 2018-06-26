package com.linguancheng.gdeiassistant.Service.CardQuery;

import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Exception.RecognitionException.RecognitionException;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Pojo.CardQuery.CardQuery;
import com.linguancheng.gdeiassistant.Pojo.CardQuery.CardQueryResult;
import com.linguancheng.gdeiassistant.Pojo.Entity.Card;
import com.linguancheng.gdeiassistant.Pojo.Entity.CardInfo;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.Recognition.RecognitionService;
import com.linguancheng.gdeiassistant.Tools.ImageEncodeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CardQueryService {

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Autowired
    private RecognitionService recognitionService;

    private Log log = LogFactory.getLog(CardQueryService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.cardquery']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 查询饭卡基本信息
     *
     * @param request
     * @param username
     * @param password
     * @return
     */
    public BaseResult<CardInfo, ServiceResultEnum> CardInfoQuery(HttpServletRequest request, String username, String password) {
        BaseResult<CardInfo, ServiceResultEnum> result = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            //登录支付管理平台
            LoginCardSystem(httpClient, username, password);
            //获取饭卡基本信息
            result.setResultData(QueryCardInformation(httpClient));
            result.setResultType(ServiceResultEnum.SUCCESS);
            return result;
        } catch (PasswordIncorrectException e) {
            log.error("查询饭卡基本信息异常：" + e);
            result.setResultType(ServiceResultEnum.PASSWORD_INCORRECT);
        } catch (ServerErrorException e) {
            log.error("查询饭卡基本信息异常：" + e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
        } catch (IOException e) {
            log.error("查询饭卡基本信息异常：" + e);
            result.setResultType(ServiceResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("查询饭卡基本信息异常" + e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
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
     * 查询消费流水
     *
     * @param request
     * @param cardQuery
     * @return
     */
    public CardQueryResult CardQuery(HttpServletRequest request, String username, String password, CardQuery cardQuery) {
        CardQueryResult cardQueryResult = new CardQueryResult();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            //登录支付管理平台
            LoginCardSystem(httpClient, username, password);
            //获取饭卡基本信息
            cardQueryResult.setCardInfo(QueryCardInformation(httpClient));
            //获取消费记录流水
            cardQueryResult.setCardList(QueryCardList(httpClient, cardQuery));
            //查询成功
            cardQueryResult.setCardQuery(cardQuery);
            cardQueryResult.setCardServiceResultEnum(ServiceResultEnum.SUCCESS);
            return cardQueryResult;
        } catch (PasswordIncorrectException e) {
            log.error("查询消费流水异常：" + e);
            cardQueryResult.setCardServiceResultEnum(ServiceResultEnum.PASSWORD_INCORRECT);
        } catch (ServerErrorException e) {
            log.error("查询消费流水异常：" + e);
            cardQueryResult.setCardServiceResultEnum(ServiceResultEnum.SERVER_ERROR);
        } catch (IOException e) {
            log.error("查询消费流水异常：" + e);
            cardQueryResult.setCardServiceResultEnum(ServiceResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("查询消费流水异常：" + e);
            cardQueryResult.setCardServiceResultEnum(ServiceResultEnum.SERVER_ERROR);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cardQueryResult;
    }

    /**
     * 校园卡挂失
     *
     * @param request
     * @param username
     * @param password
     * @param cardPassword
     * @return
     */
    public BaseResult<String, ServiceResultEnum> CardLost(HttpServletRequest request, String username, String password, String cardPassword) {
        BaseResult<String, ServiceResultEnum> result = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            //登录支付管理平台
            LoginCardSystem(httpClient, username, password);
            BaseResult<String, BoolResultEnum> submitResult = SubmitCardLostRequest(httpClient, cardPassword);
            switch (submitResult.getResultType()) {
                case SUCCESS:
                    result.setResultType(ServiceResultEnum.SUCCESS);
                    break;

                case ERROR:
                    result.setResultType(ServiceResultEnum.ERROR_CONDITION);
                    result.setResultData(submitResult.getResultData());
                    break;
            }
        } catch (PasswordIncorrectException e) {
            log.error("校园卡挂失异常：" + e);
            result.setResultType(ServiceResultEnum.PASSWORD_INCORRECT);
        } catch (IOException e) {
            log.error("校园卡挂失异常：" + e);
            result.setResultType(ServiceResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("校园卡挂失异常：" + e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
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
    public void LoginCardSystem(CloseableHttpClient httpClient, String username, String password) throws IOException, ServerErrorException, PasswordIncorrectException {
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
                    return;
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
                return;
            }
            throw new ServerErrorException("支付管理平台异常");
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
     * @throws IOException
     */
    private BaseResult<String, BoolResultEnum> SubmitCardLostRequest(CloseableHttpClient httpClient, String cardPassword) throws ServerErrorException, RecognitionException, IOException {
        BaseResult<String, BoolResultEnum> result = new BaseResult<>();
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
                                    , RecognitionService.CheckCodeTypeEnum.NUMBER, 4);
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
                                JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                                if (jsonObject.has("ret") && jsonObject.has("msg")) {
                                    if (jsonObject.getBoolean("ret")) {
                                        result.setResultType(BoolResultEnum.SUCCESS);
                                        return result;
                                    } else {
                                        String message = jsonObject.getString("msg");
                                        if ("验证码不正确".equals(message)) {
                                            throw new RecognitionException("识别验证码图片失败");
                                        } else {
                                            result.setResultType(BoolResultEnum.ERROR);
                                            result.setResultData(message);
                                            return result;
                                        }
                                    }
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
                    int listsCount = Integer.valueOf(strings[2].substring(6, strings[2].length()));
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
                    int listsCount = Integer.valueOf(strings[4].substring(6, strings[4].length()));
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
