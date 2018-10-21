package com.linguancheng.gdeiassistant.Service.SpareRoom;

import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Exception.QueryException.TimeStampIncorrectException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Pojo.Entity.SpareRoom;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.SpareRoomQuery.SpareRoomQuery;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.linguancheng.gdeiassistant.Repository.Redis.User.UserDao;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
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
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpareRoomService {

    private String url;

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserLoginService userLoginService;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }

    private Log log = LogFactory.getLog(SpareRoomService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.spareroom']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 查询空课室信息
     *
     * @param request
     * @param username
     * @param keycode
     * @param number
     * @return
     */
    private BaseResult<List<SpareRoom>, ServiceResultEnum> QuerySpareRoom(HttpServletRequest request
            , String username, String keycode, String number, Long timestamp, SpareRoomQuery spareRoomQuery) {
        BaseResult<List<SpareRoom>, ServiceResultEnum> baseResult = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), false, timeout);
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
                            Elements kssjOptions = document.getElementById("kssj").select("option");
                            Elements jssjOptions = document.getElementById("jssj").select("option");
                            if (spareRoomQuery.getStartTime() >= kssjOptions.size()) {
                                //当前没有可选择的日期
                                throw new ErrorQueryConditionException("没有可选择的日期");
                            }
                            //激活指定日期的周数和星期选项
                            HttpPost httpPost = new HttpPost(url + "xxjsjy.aspx?xh=" + number);
                            List<BasicNameValuePair> initBasicNameValuePairList = new ArrayList<>();
                            initBasicNameValuePairList.add(new BasicNameValuePair("__EVENTTARGET", "jssj"));
                            initBasicNameValuePairList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
                            initBasicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", document.getElementsByAttributeValue("name", "__VIEWSTATE").val()));
                            initBasicNameValuePairList.add(new BasicNameValuePair("xiaoq", ""));
                            initBasicNameValuePairList.add(new BasicNameValuePair("jslb", ""));
                            initBasicNameValuePairList.add(new BasicNameValuePair("kssj", kssjOptions.get(spareRoomQuery.getStartTime()).attr("value")));
                            if (jssjOptions.size() <= spareRoomQuery.getEndTime()) {
                                spareRoomQuery.setEndTime(jssjOptions.size() - 1);
                            }
                            initBasicNameValuePairList.add(new BasicNameValuePair("jssj", jssjOptions.get(spareRoomQuery.getEndTime()).attr("value")));
                            initBasicNameValuePairList.add(new BasicNameValuePair("xqj", document.getElementById("xqj").select("option").first().attr("value")));
                            initBasicNameValuePairList.add(new BasicNameValuePair("ddlDsz", document.getElementById("ddlDsz").select("option").first().attr("value")));
                            initBasicNameValuePairList.add(new BasicNameValuePair("sjd", document.getElementById("sjd").select("option").first().attr("value")));
                            initBasicNameValuePairList.add(new BasicNameValuePair("xn", document.getElementById("xn").select("option").last().attr("value")));
                            initBasicNameValuePairList.add(new BasicNameValuePair("xq", document.getElementById("xq").select("option").first().attr("value")));
                            initBasicNameValuePairList.add(new BasicNameValuePair("ddlSyXn", document.getElementById("ddlSyXn").select("option").last().attr("value")));
                            initBasicNameValuePairList.add(new BasicNameValuePair("ddlSyxq", document.getElementById("ddlSyxq").select("option").first().attr("value")));
                            if (spareRoomQuery.getMinSeating() != null) {
                                initBasicNameValuePairList.add(new BasicNameValuePair("min_zws", String.valueOf(spareRoomQuery.getMinSeating())));
                            } else {
                                initBasicNameValuePairList.add(new BasicNameValuePair("min_zws", ""));
                            }
                            if (spareRoomQuery.getMaxSeating() != null) {
                                initBasicNameValuePairList.add(new BasicNameValuePair("max_zws", String.valueOf(spareRoomQuery.getMaxSeating())));
                            } else {
                                initBasicNameValuePairList.add(new BasicNameValuePair("max_zws", String.valueOf(spareRoomQuery.getMaxSeating())));
                            }
                            //绑定请求参数
                            httpPost.setEntity(new UrlEncodedFormEntity(initBasicNameValuePairList, StandardCharsets.UTF_8));
                            httpResponse = httpClient.execute(httpPost);
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
                                    String min_zws = spareRoomQuery.getMinSeating() == null ? "" : String.valueOf(spareRoomQuery.getMinSeating());
                                    String max_zws = spareRoomQuery.getMaxSeating() == null ? "" : String.valueOf(spareRoomQuery.getMaxSeating());
                                    String kssj = Form1.getElementById("kssj").select("option").get(spareRoomQuery.getStartTime()).attr("value");
                                    String jssj = Form1.getElementById("jssj").select("option").get(spareRoomQuery.getEndTime()).attr("value");
                                    Elements xqjOptions = Form1.getElementById("xqj").select("option");
                                    String xqj = null;
                                    for (Element xqjOption : xqjOptions) {
                                        if (Integer.valueOf(xqjOption.attr("value")) == spareRoomQuery.getWeek() + 1) {
                                            xqj = xqjOption.attr("value");
                                        }
                                    }
                                    if (xqj == null) {
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
                                    httpPost = new HttpPost(url + "xxjsjy.aspx?xh=" + number);
                                    List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                                    basicNameValuePairList.add(new BasicNameValuePair("__EVENTTARGET", __EVENTTARGET));
                                    basicNameValuePairList.add(new BasicNameValuePair("__EVENTARGUMENT", __EVENTARGUMENT));
                                    basicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
                                    basicNameValuePairList.add(new BasicNameValuePair("xiaoq", xiaoq));
                                    basicNameValuePairList.add(new BasicNameValuePair("jslb", jslb));
                                    basicNameValuePairList.add(new BasicNameValuePair("min_zws", min_zws));
                                    basicNameValuePairList.add(new BasicNameValuePair("max_zws", max_zws));
                                    basicNameValuePairList.add(new BasicNameValuePair("kssj", kssj));
                                    basicNameValuePairList.add(new BasicNameValuePair("jssj", jssj));
                                    basicNameValuePairList.add(new BasicNameValuePair("xqj", xqj));
                                    basicNameValuePairList.add(new BasicNameValuePair("ddlDsz", ddlDsz));
                                    basicNameValuePairList.add(new BasicNameValuePair("sjd", sjd));
                                    if (pageIndex == 1) {
                                        basicNameValuePairList.add(new BasicNameValuePair("dpDataGrid1:txtChoosePage", String.valueOf(pageIndex)));
                                        basicNameValuePairList.add(new BasicNameValuePair("Button2", "空教室查询"));
                                    } else {
                                        basicNameValuePairList.add(new BasicNameValuePair("dpDataGrid1:txtChoosePage", String.valueOf(pageIndex - 1)));
                                        basicNameValuePairList.add(new BasicNameValuePair("dpDataGrid1:btnNextPage", "下一页"));
                                    }
                                    basicNameValuePairList.add(new BasicNameValuePair("dpDataGrid1:txtPageSize", "40"));
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
                                            baseResult.setResultType(ServiceResultEnum.EMPTY_RESULT);
                                            return baseResult;
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
                                baseResult.setResultData(spareRoomList);
                                baseResult.setResultType(ServiceResultEnum.SUCCESS);
                                return baseResult;
                            }
                            throw new ServerErrorException("教务系统异常");
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
            log.error("查询空课室异常：", e);
            baseResult.setResultType(ServiceResultEnum.TIME_OUT);
        } catch (TimeStampIncorrectException e) {
            log.error("查询空课室异常：", e);
            baseResult.setResultType(ServiceResultEnum.TIMESTAMP_INVALID);
        } catch (ServerErrorException e) {
            log.error("查询空课室异常：", e);
            baseResult.setResultType(ServiceResultEnum.SERVER_ERROR);
        } catch (ErrorQueryConditionException e) {
            log.error("查询空课室异常：", e);
            baseResult.setResultType(ServiceResultEnum.ERROR_CONDITION);
        } catch (Exception e) {
            log.error("查询空课室异常：", e);
            baseResult.setResultType(ServiceResultEnum.SERVER_ERROR);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return baseResult;
    }

    /**
     * 与教务系统进行会话同步并查询空教室信息
     *
     * @param request
     * @param user
     * @param spareRoomQuery
     * @return
     */
    public BaseResult<List<SpareRoom>, ServiceResultEnum> SyncSessionAndQuerySpareRoom(HttpServletRequest request
            , User user, SpareRoomQuery spareRoomQuery) {
        BaseResult<List<SpareRoom>, ServiceResultEnum> result = new BaseResult<>();
        UserCertificate userCertificate = userDao.queryUserCertificate(user.getUsername());
        //检测是否已与教务系统进行会话同步
        if (userCertificate == null) {
            //进行会话同步
            BaseResult<UserCertificate, LoginResultEnum> userLoginResult = userLoginService
                    .UserLogin(request, user, false);
            switch (userLoginResult.getResultType()) {
                case LOGIN_SUCCESS:
                    Long timestamp = userLoginResult.getResultData().getTimestamp();
                    if (StringUtils.isBlank(user.getKeycode())) {
                        user.setKeycode(userLoginResult.getResultData().getUser().getKeycode());
                    }
                    if (StringUtils.isBlank(user.getNumber())) {
                        user.setNumber(userLoginResult.getResultData().getUser().getNumber());
                    }
                    userCertificate = new UserCertificate();
                    userCertificate.setUser(user);
                    userCertificate.setTimestamp(timestamp);
                    userDao.saveUserCertificate(userCertificate);
                    return QuerySpareRoom(request, user.getUsername()
                            , user.getKeycode(), user.getNumber(), timestamp, spareRoomQuery);

                case TIME_OUT:
                    result.setResultType(ServiceResultEnum.TIME_OUT);
                    break;

                case PASSWORD_ERROR:
                    result.setResultType(ServiceResultEnum.PASSWORD_INCORRECT);
                    break;

                case SERVER_ERROR:
                    result.setResultType(ServiceResultEnum.SERVER_ERROR);
                    break;
            }
            return result;
        }
        return QuerySpareRoom(request, user.getUsername(), user.getKeycode(), user.getNumber()
                , userCertificate.getTimestamp(), spareRoomQuery);
    }
}
