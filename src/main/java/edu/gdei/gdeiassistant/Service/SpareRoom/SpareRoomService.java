package edu.gdei.gdeiassistant.Service.SpareRoom;

import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import edu.gdei.gdeiassistant.Exception.QueryException.TimeStampIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Entity.SpareRoom;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import edu.gdei.gdeiassistant.Pojo.SpareRoomQuery.SpareRoomQuery;
import edu.gdei.gdeiassistant.Pojo.UserLogin.UserCertificate;
import edu.gdei.gdeiassistant.Repository.Redis.UserCertificate.UserCertificateDao;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpareRoomService {

    private String url;

    @Autowired
    private UserCertificateDao userCertificateDao;

    @Autowired
    private UserLoginService userLoginService;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }

    private Logger logger = LoggerFactory.getLogger(SpareRoomService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.spareroom']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 查询空课室信息
     *
     * @param sessionId
     * @param username
     * @param keycode
     * @param number
     * @return
     */
    private List<SpareRoom> QuerySpareRoom(String sessionId, String username, String keycode, String number, Long timestamp, SpareRoomQuery spareRoomQuery) throws NetWorkTimeoutException, TimeStampIncorrectException, ServerErrorException, ErrorQueryConditionException {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId
                    , false, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            HttpGet httpGet = new HttpGet(url + "cas_verify.aspx?i=" + username + "&k="
                    + keycode + "&timestamp=" + timestamp);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (document.toString().equals("您登陆的系统已经很长时间没有操作了，为安全起见请重新登录后再进行操作！")) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                } else {
                    //进入教务系统个人主页
                    httpGet = new HttpGet(url + "xs_main.aspx?xh=" + number);
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //成功进入学生个人主页，进入空课室查询页面
                        httpGet = new HttpGet(url + "xxjsjy.aspx?xh=" + number);
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            //逐页提交查询请求并获取数据
                            int pageIndex = 1;
                            int maxIndex = 1;
                            List<SpareRoom> spareRoomList = new ArrayList<>();
                            while (pageIndex <= maxIndex) {
                                Element Form1 = document.getElementsByAttributeValue("name", "Form1").first();
                                String __EVENTTARGET = Form1.getElementsByAttributeValue("name", "__EVENTTARGET").first().val();
                                String __EVENTARGUMENT = Form1.getElementsByAttributeValue("name", "__EVENTARGUMENT").first().val();
                                String __VIEWSTATE = Form1.getElementsByAttributeValue("name", "__VIEWSTATE").first().val();
                                String xiaoq = Form1.getElementById("xiaoq").select("option").get(spareRoomQuery.getZone()).attr("value");
                                String jslb = Form1.getElementById("jslb").select("option").get(spareRoomQuery.getType()).attr("value");
                                String min_zws = spareRoomQuery.getMinSeating() == null ? "0" : String.valueOf(spareRoomQuery.getMinSeating());
                                String max_zws = spareRoomQuery.getMaxSeating() == null ? "" : String.valueOf(spareRoomQuery.getMaxSeating());
                                String ddlKsz = String.valueOf(spareRoomQuery.getStartTime());
                                String ddlJsz = String.valueOf(spareRoomQuery.getEndTime());
                                Elements xqjOptions = Form1.getElementById("xqj").select("option");
                                String xqj = null;
                                for (Element xqjOption : xqjOptions) {
                                    if (Integer.valueOf(xqjOption.attr("value")) == spareRoomQuery.getMinWeek() + 1) {
                                        xqj = xqjOption.attr("value");
                                    }
                                }
                                if (xqj == null) {
                                    throw new ErrorQueryConditionException("错误的查询条件");
                                }
                                String ddlXqm = null;
                                for (Element xqjOption : xqjOptions) {
                                    if (Integer.valueOf(xqjOption.attr("value")) == spareRoomQuery.getMaxWeek() + 1) {
                                        ddlXqm = xqjOption.attr("value");
                                    }
                                }
                                if (ddlXqm == null) {
                                    throw new ErrorQueryConditionException("错误的查询条件");
                                }
                                String weekTypeString = null;
                                switch (spareRoomQuery.getWeekType()) {
                                    case 0:
                                        weekTypeString = "";
                                        break;

                                    case 1:
                                        weekTypeString = "单";
                                        break;

                                    case 2:
                                        weekTypeString = "双";
                                        break;
                                }
                                String ddlDsz = null;
                                Elements ddlDszOptions = Form1.getElementById("ddlDsz").select("option");
                                if (ddlDszOptions.size() == 3) {
                                    for (Element ddlDszOption : ddlDszOptions) {
                                        if (ddlDszOption.attr("value").equals(weekTypeString)) {
                                            ddlDsz = ddlDszOption.attr("value");
                                            break;
                                        }
                                    }
                                } else {
                                    ddlDsz = ddlDszOptions.first().attr("value");
                                }
                                if (ddlDsz == null) {
                                    throw new ErrorQueryConditionException("错误的查询条件");
                                }
                                String sjd = Form1.getElementById("sjd").select("option").get(spareRoomQuery.getClassNumber()).attr("value");
                                String ddlSyXn = null;
                                Elements ddlSyXnOptions = document.getElementById("ddlSyXn").select("option");
                                for (Element ddlSyXnOption : ddlSyXnOptions) {
                                    if (ddlSyXnOption.hasAttr("selected") && ddlSyXnOption.attr("selected").equals("selected")) {
                                        ddlSyXn = ddlSyXnOption.attr("value");
                                    }
                                }
                                String ddlSyxq = null;
                                Elements ddlSyxqOptions = document.getElementById("ddlSyxq").select("option");
                                for (Element ddlSyxqOption : ddlSyxqOptions) {
                                    if (ddlSyxqOption.hasAttr("selected") && ddlSyxqOption.attr("selected").equals("selected")) {
                                        ddlSyxq = ddlSyxqOption.attr("value");
                                    }
                                }
                                String xn = null;
                                Elements xnOptions = document.getElementById("xn").select("option");
                                for (Element xnOption : xnOptions) {
                                    if (xnOption.hasAttr("selected") && xnOption.attr("selected").equals("selected")) {
                                        xn = xnOption.attr("value");
                                    }
                                }
                                String xq = null;
                                Elements xqOptions = document.getElementById("xq").select("option");
                                for (Element xqOption : xqOptions) {
                                    if (xqOption.hasAttr("selected") && xqOption.attr("selected").equals("selected")) {
                                        xq = xqOption.attr("value");
                                    }
                                }
                                //构建POST请求体
                                HttpPost httpPost = new HttpPost(url + document.getElementsByAttributeValue("name", "Form1").first().attr("action"));
                                List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                                basicNameValuePairList.add(new BasicNameValuePair("__EVENTTARGET", __EVENTTARGET));
                                basicNameValuePairList.add(new BasicNameValuePair("__EVENTARGUMENT", __EVENTARGUMENT));
                                basicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
                                basicNameValuePairList.add(new BasicNameValuePair("xiaoq", xiaoq));
                                basicNameValuePairList.add(new BasicNameValuePair("jslb", jslb));
                                basicNameValuePairList.add(new BasicNameValuePair("min_zws", min_zws));
                                basicNameValuePairList.add(new BasicNameValuePair("max_zws", max_zws));
                                basicNameValuePairList.add(new BasicNameValuePair("ddlKsz", ddlKsz));
                                basicNameValuePairList.add(new BasicNameValuePair("ddlJsz", ddlJsz));
                                basicNameValuePairList.add(new BasicNameValuePair("xqj", xqj));
                                if (ddlKsz.equals(ddlJsz)) {
                                    basicNameValuePairList.add(new BasicNameValuePair("ddlXqm", ddlXqm));
                                }
                                basicNameValuePairList.add(new BasicNameValuePair("ddlDsz", ddlDsz));
                                basicNameValuePairList.add(new BasicNameValuePair("sjd", sjd));
                                if (pageIndex == 1) {
                                    basicNameValuePairList.add(new BasicNameValuePair("Button2", "空教室查询"));
                                } else {
                                    basicNameValuePairList.add(new BasicNameValuePair("dpDataGrid1:txtChoosePage", String.valueOf(pageIndex - 1)));
                                    basicNameValuePairList.add(new BasicNameValuePair("dpDataGrid1:btnNextPage", "下一页"));
                                    basicNameValuePairList.add(new BasicNameValuePair("dpDataGrid1:txtPageSize", "40"));
                                }
                                basicNameValuePairList.add(new BasicNameValuePair("ddlSyXn", ddlSyXn));
                                basicNameValuePairList.add(new BasicNameValuePair("ddlSyxq", ddlSyxq));
                                basicNameValuePairList.add(new BasicNameValuePair("xn", xn));
                                basicNameValuePairList.add(new BasicNameValuePair("xq", xq));
                                //绑定请求参数
                                httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                                httpResponse = httpClient.execute(httpPost);
                                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                    Elements trs = document.select("table").first().select("tr");
                                    if (trs.size() <= 1) {
                                        return null;
                                    }
                                    if (pageIndex == 1) {
                                        maxIndex = Integer.valueOf(document.getElementById("dpDataGrid1_lblTotalPages").text());
                                    }
                                    for (int j = 1; j < trs.size(); j++) {
                                        Elements tds = trs.get(j).select("td");
                                        SpareRoom spareRoom = new SpareRoom();
                                        spareRoom.setNumber(tds.first().text());
                                        spareRoom.setName(tds.get(1).text());
                                        spareRoom.setType(tds.get(2).text());
                                        switch (Integer.valueOf(tds.get(3).text())) {
                                            case 1:
                                                spareRoom.setZone("海珠");
                                                break;

                                            case 4:
                                                spareRoom.setZone("花都");
                                                break;

                                            case 8:
                                                spareRoom.setZone("广东轻工南海校区");
                                                break;

                                            case 9:
                                                spareRoom.setZone("业余函授校区");
                                                break;

                                            default:
                                                spareRoom.setZone("其他");
                                                break;
                                        }
                                        spareRoom.setClassSeating(tds.get(4).text());
                                        spareRoom.setSection(tds.get(5).text());
                                        spareRoom.setExamSeating(tds.get(6).text());
                                        spareRoomList.add(spareRoom);
                                    }
                                    pageIndex++;
                                    continue;
                                }
                                throw new ServerErrorException("教务系统异常");
                            }
                            return spareRoomList;
                        }
                        throw new ServerErrorException("教务系统异常");
                    }
                    throw new ServerErrorException("教务系统异常");
                }
            } else if (httpResponse.getStatusLine().getStatusCode() == 302) {
                if (httpResponse.getFirstHeader("Location").getValue()
                        .equals("/loginTs/loginTs_yzsb.html")) {
                    //时间戳校验失败
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                throw new PasswordIncorrectException("账号密码错误");
            }
            throw new ServerErrorException("教务系统异常");
        } catch (IOException e) {
            logger.error("查询空课室异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (TimeStampIncorrectException e) {
            logger.error("查询空课室异常：", e);
            throw new TimeStampIncorrectException("时间戳校验失败");
        } catch (ErrorQueryConditionException e) {
            logger.error("查询空课室异常：", e);
            throw new ErrorQueryConditionException("查询条件错误");
        } catch (Exception e) {
            logger.error("查询空课室异常：", e);
            throw new ServerErrorException("教务系统异常");
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
     * 与教务系统进行会话同步并查询空教室信息
     *
     * @param sessionId
     * @param user
     * @param spareRoomQuery
     * @return
     */
    public List<SpareRoom> SyncSessionAndQuerySpareRoom(String sessionId, User user, SpareRoomQuery spareRoomQuery) throws Exception {
        UserCertificate userCertificate = userCertificateDao.queryUserCertificate(user.getUsername());
        //检测是否已与教务系统进行会话同步
        if (userCertificate == null) {
            //进行会话同步
            userCertificate = userLoginService.SyncUpdateSession(sessionId, user);
            return QuerySpareRoom(sessionId, userCertificate.getUser().getUsername()
                    , userCertificate.getUser().getKeycode(), userCertificate.getUser().getNumber(), userCertificate.getTimestamp(), spareRoomQuery);
        }
        return QuerySpareRoom(sessionId, userCertificate.getUser().getUsername(), userCertificate.getUser().getKeycode()
                , userCertificate.getUser().getNumber(), userCertificate.getTimestamp(), spareRoomQuery);
    }
}
