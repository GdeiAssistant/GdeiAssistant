package edu.gdei.gdeiassistant.Service.Evaluate;

import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.EvaluateException.NotAvailableTimeException;
import edu.gdei.gdeiassistant.Exception.QueryException.TimeStampIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
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
public class EvaluateService {

    private Logger logger = LoggerFactory.getLogger(EvaluateService.class);

    private String url;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserCertificateDao userCertificateDao;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }


    private int timeout;

    @Value("#{propertiesReader['timeout.evaluate']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 调用HttpClient完成教学质量评价
     *
     * @param httpClient
     * @param username
     * @param keycode
     * @param number
     * @return
     * @throws IOException
     * @throws ServerErrorException
     * @throws NotAvailableTimeException
     */
    private void TeacherEvaluate(CloseableHttpClient httpClient, String username
            , String keycode, String number, Long timestamp, boolean directlySubmit) throws IOException, ServerErrorException, NotAvailableTimeException, TimeStampIncorrectException {
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
                    //成功进入学生个人主页,获取所有评教列表
                    Elements tops = document.getElementsByClass("nav").first().getElementsByClass("top");
                    for (Element top : tops) {
                        //选择教学评价的菜单
                        if (top.select("span").first().text().contains("教学评价")) {
                            Element sub = top.getElementsByClass("sub").first();
                            if (sub != null) {
                                Elements lis = sub.select("li");
                                //缓存所有需要评教课程的URL列表
                                List<String> evaluationList = new ArrayList<>();
                                if (lis != null && lis.size() > 0) {
                                    for (Element li : lis) {
                                        evaluationList.add(li.select("a").first().attr("href"));
                                    }
                                    //进入教学评价页面
                                    httpGet = new HttpGet(url + evaluationList.get(0));
                                    httpResponse = httpClient.execute(httpGet);
                                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                        //填写所有的评教信息并保存
                                        for (String url : evaluationList) {
                                            //构建需要POST提交的Form表单
                                            List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                                            //获取隐藏域的数据值
                                            String __EVENTTARGET = document.getElementsByAttributeValue("name", "__EVENTTARGET").val();
                                            String __EVENTARGUMENT = document.getElementsByAttributeValue("name", "__EVENTARGUMENT").val();
                                            String __VIEWSTATE = document.getElementsByAttributeValue("name", "__VIEWSTATE").val();
                                            //添加隐藏域的数据到表单中
                                            basicNameValuePairList.add(new BasicNameValuePair("__EVENTTARGET", __EVENTTARGET));
                                            basicNameValuePairList.add(new BasicNameValuePair("__EVENTARGUMENT", __EVENTARGUMENT));
                                            basicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
                                            //添加当前评教课程值到表单中
                                            String currentClass = ((url.split("&"))[0].split("="))[1];
                                            basicNameValuePairList.add(new BasicNameValuePair("pjkc", currentClass));
                                            //获取存放评教信息的表格
                                            Element table = document.getElementById("Table2");
                                            //获取表格中需要提交的输入框
                                            Elements inputs = table.select("input");
                                            //获取表格中需要提交的选择框
                                            Elements selects = table.select("select");
                                            for (Element input : inputs) {
                                                basicNameValuePairList.add(new BasicNameValuePair(input.attr("name"), input.val()));
                                            }
                                            for (Element select : selects) {
                                                //评教等级选择框
                                                if (!select.attr("name").equals("pjkc")) {
                                                    basicNameValuePairList.add(new BasicNameValuePair(select.attr("name"), "优秀"));
                                                }
                                            }
                                            //添加其余的参数到Form表单中
                                            basicNameValuePairList.add(new BasicNameValuePair("pjxx", ""));
                                            basicNameValuePairList.add(new BasicNameValuePair("txt1", ""));
                                            basicNameValuePairList.add(new BasicNameValuePair("TextBox1", "0"));
                                            basicNameValuePairList.add(new BasicNameValuePair("Button1", "保  存"));
                                            String postUrl = this.url + document.getElementById("Form1").attr("action");
                                            HttpPost httpPost = new HttpPost(postUrl);
                                            //绑定表单参数
                                            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                                            //提交评教请求
                                            httpResponse = httpClient.execute(httpPost);
                                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                                            } else {
                                                throw new ServerErrorException("教务系统异常");
                                            }
                                        }
                                        //如果用户需要直接提交教学质量评价信息,则进行提交
                                        if (directlySubmit) {
                                            //构建需要POST提交的Form表单
                                            List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                                            //获取隐藏域的数据值
                                            String __EVENTTARGET = document.getElementsByAttributeValue("name", "__EVENTTARGET").val();
                                            String __EVENTARGUMENT = document.getElementsByAttributeValue("name", "__EVENTARGUMENT").val();
                                            String __VIEWSTATE = document.getElementsByAttributeValue("name", "__VIEWSTATE").val();
                                            //添加隐藏域的数据到表单中
                                            basicNameValuePairList.add(new BasicNameValuePair("__EVENTTARGET", __EVENTTARGET));
                                            basicNameValuePairList.add(new BasicNameValuePair("__EVENTARGUMENT", __EVENTARGUMENT));
                                            basicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
                                            //添加当前评教课程值到表单中
                                            String currentClass = ((evaluationList.get(evaluationList.size() - 1).split("&"))[0].split("="))[1];
                                            basicNameValuePairList.add(new BasicNameValuePair("pjkc", currentClass));
                                            //获取存放评教信息的表格
                                            Element table = document.getElementById("Table2");
                                            //获取表格中需要提交的输入框
                                            Elements inputs = table.select("input");
                                            //获取表格中需要提交的选择框
                                            Elements selects = table.select("select");
                                            for (Element input : inputs) {
                                                basicNameValuePairList.add(new BasicNameValuePair(input.attr("name"), input.val()));
                                            }
                                            for (Element select : selects) {
                                                //评教等级选择框
                                                if (!select.attr("name").equals("pjkc")) {
                                                    basicNameValuePairList.add(new BasicNameValuePair(select.attr("name"), "优秀"));
                                                }
                                            }
                                            //添加其余的参数到Form表单中
                                            basicNameValuePairList.add(new BasicNameValuePair("pjxx", ""));
                                            basicNameValuePairList.add(new BasicNameValuePair("txt1", ""));
                                            basicNameValuePairList.add(new BasicNameValuePair("TextBox1", "0"));
                                            basicNameValuePairList.add(new BasicNameValuePair("Button2", "提  交"));
                                            String postUrl = this.url + document.getElementById("Form1").attr("action");
                                            HttpPost httpPost = new HttpPost(postUrl);
                                            //绑定表单参数
                                            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                                            //最终提交评教请求
                                            httpResponse = httpClient.execute(httpPost);
                                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                                return;
                                            }
                                            throw new ServerErrorException("教务系统异常");
                                        }
                                        return;
                                    }
                                    throw new ServerErrorException("教务系统异常");
                                }
                                throw new NotAvailableTimeException("不是教学评价开放时间");
                            }
                            throw new NotAvailableTimeException("不是教学评价开放时间");
                        }
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
        }
        throw new ServerErrorException("教务系统异常");
    }

    /**
     * 一键评教接口
     *
     * @param sessionId
     * @param username
     * @param keycode
     * @param number
     */
    private void TeacherEvaluate(String sessionId
            , String username, String keycode, String number, Long timestamp, boolean directlySubmit) throws NetWorkTimeoutException, TimeStampIncorrectException, NotAvailableTimeException, ServerErrorException {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            TeacherEvaluate(httpClient, username, keycode, number, timestamp, directlySubmit);
        } catch (IOException e) {
            logger.error("一键评教异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (TimeStampIncorrectException e) {
            logger.error("一键评教异常：", e);
            throw new TimeStampIncorrectException("时间戳校验失败");
        } catch (NotAvailableTimeException e) {
            logger.error("一键评教异常：", e);
            throw new NotAvailableTimeException("现在不是一键评教开放时间段");
        } catch (Exception e) {
            logger.error("一键评教异常：", e);
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
     * 与教务系统进行会话同步，并进行一键评教
     *
     * @param sessionId
     * @param user
     * @param directlySubmit
     * @return
     */
    public void SyncSessionAndEvaluate(String sessionId, User user
            , boolean directlySubmit) throws Exception {
        UserCertificate userCertificate = userCertificateDao.queryUserCertificate(user.getUsername());
        //检测是否已与教务系统进行会话同步
        if (userCertificate == null) {
            //进行会话同步
            userCertificate = userLoginService.SyncUpdateSession(sessionId, user);
            TeacherEvaluate(sessionId, user.getUsername()
                    , user.getKeycode(), user.getNumber(), userCertificate.getTimestamp(), directlySubmit);
        }
        TeacherEvaluate(sessionId, userCertificate.getUser().getUsername()
                , userCertificate.getUser().getKeycode(), userCertificate.getUser().getNumber()
                , userCertificate.getTimestamp(), directlySubmit);
    }
}
