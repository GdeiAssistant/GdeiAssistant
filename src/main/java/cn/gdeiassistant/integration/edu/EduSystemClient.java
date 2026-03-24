package cn.gdeiassistant.integration.edu;

import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.common.tools.Utils.WeekUtils;
import cn.gdeiassistant.integration.edu.pojo.EduSessionCredential;
import cn.gdeiassistant.integration.httpclient.HttpClientSession;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 教务系统网络防腐层：专职处理对 jwgl.gdei.edu.cn 的 HTTP 请求，返回解析好的 Document 或数据，供 core 业务层使用。
 */
@Component
public class EduSystemClient {

    private static final String JWGL_BASE = "http://jwgl.gdei.edu.cn";
    private static final int EDU_REQUEST_TIMEOUT_SEC = 15;

    /**
     * 时间戳校验 + 进入学生主页 + 打开成绩查询页（GET），返回成绩列表页 Document，供 Service 解析学年列表与 __VIEWSTATE。
     *
     * @param sessionId 会话 ID（用于 HttpClient 与 Cookie 同步）
     * @param credential 教务会话凭证
     * @return 成绩查询列表页 Jsoup Document
     */
    public Document fetchGradeListPage(String sessionId, EduSessionCredential credential)
            throws IOException, TimeStampIncorrectException, PasswordIncorrectException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, EDU_REQUEST_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(JWGL_BASE + "/cas_verify.aspx?i=" + credential.getUsername()
                    + "&k=" + credential.getKeycode() + "&timestamp=" + credential.getTimestamp());
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (document.toString().contains("您登陆的系统已经很长时间没有操作了")
                        && document.toString().contains("为安全起见请重新登录后再进行操作！")) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                httpGet = new HttpGet(JWGL_BASE + "/xs_main.aspx?xh=" + credential.getNumber() + "&type=1");
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new ServerErrorException("教务系统异常");
                }
                httpGet = new HttpGet(JWGL_BASE + "/xscj_gc.aspx?xh=" + credential.getNumber());
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new ServerErrorException("教务系统异常");
                }
                return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            }
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                String location = httpResponse.getFirstHeader("Location") != null
                        ? httpResponse.getFirstHeader("Location").getValue() : "";
                if ("/loginTs/loginTs_yzsb.html".equals(location)) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                throw new PasswordIncorrectException("账号密码错误");
            }
            throw new ServerErrorException("教务系统异常");
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 按学年提交成绩查询（POST），返回该学年成绩表页 Document。调用前需已通过 fetchGradeListPage 完成校验并拿到 viewState、yearOptionValue。
     *
     * @param sessionId 会话 ID
     * @param credential 教务会话凭证
     * @param viewState  __VIEWSTATE 值
     * @param yearOptionValue 学年下拉框选项 value（如 "2024-2025"）
     * @return 该学年成绩表页 Jsoup Document（含 datelist 表格）
     */
    public Document fetchGradeByYear(String sessionId, EduSessionCredential credential,
                                     String viewState, String yearOptionValue)
            throws IOException, PasswordIncorrectException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, EDU_REQUEST_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            List<BasicNameValuePair> form = new ArrayList<>();
            form.add(new BasicNameValuePair("__VIEWSTATE", viewState));
            form.add(new BasicNameValuePair("Button5", "按学年查询"));
            form.add(new BasicNameValuePair("ddlXN", yearOptionValue));
            form.add(new BasicNameValuePair("ddlXQ", ""));
            HttpPost httpPost = new HttpPost(JWGL_BASE + "/xscj_gc.aspx?xh=" + credential.getNumber());
            httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("教务系统异常");
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 时间戳校验 + 进入学生主页 + 打开课表查询页；若当前默认学年学期与配置的当前学年学期不一致，则 POST 切换到当前学期，返回最终课表页 Document。
     *
     * @param sessionId 会话 ID
     * @param credential 教务会话凭证
     * @return 课表页 Jsoup Document（含 Table1）
     */
    public Document fetchScheduleDocument(String sessionId, EduSessionCredential credential)
            throws IOException, TimeStampIncorrectException, PasswordIncorrectException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, EDU_REQUEST_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(JWGL_BASE + "/cas_verify.aspx?i=" + credential.getUsername()
                    + "&k=" + credential.getKeycode() + "&timestamp=" + credential.getTimestamp());
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (document.toString().equals("您登陆的系统已经很长时间没有操作了，为安全起见请重新登录后再进行操作！")) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                httpGet = new HttpGet(JWGL_BASE + "/xs_main.aspx?xh=" + credential.getNumber() + "&type=1");
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new ServerErrorException("教务系统异常");
                }
                httpGet = new HttpGet(JWGL_BASE + "/xskbcx.aspx?xh=" + credential.getNumber());
                httpResponse = httpClient.execute(httpGet);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()).replace("<br>", "$info$"));
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new ServerErrorException("教务系统异常");
                }
                Element xqdElement = document.getElementsByAttributeValue("name", "xqd").first();
                if (xqdElement == null) {
                    throw new ServerErrorException("教务系统页面结构异常，未找到学期下拉框");
                }
                Elements xqdOptions = xqdElement.select("option");
                int defaultTerm = 0;
                for (Element option : xqdOptions) {
                    if (option.hasAttr("selected")) {
                        defaultTerm = Integer.parseInt(option.attr("value"));
                        break;
                    }
                }
                Element xndElement = document.getElementsByAttributeValue("name", "xnd").first();
                if (xndElement == null) {
                    throw new ServerErrorException("教务系统页面结构异常，未找到学年下拉框");
                }
                Elements xndOptions = xndElement.select("option");
                int defaultYear = 0;
                for (Element option : xndOptions) {
                    if (option.hasAttr("selected")) {
                        defaultYear = Integer.parseInt(option.attr("value").split("-")[0]);
                        break;
                    }
                }
                if (defaultYear != WeekUtils.getCurrentYear() || defaultTerm != WeekUtils.getCurrentTerm()) {
                    Element viewStateElement = document.getElementsByAttributeValue("name", "__VIEWSTATE").first();
                    if (viewStateElement == null) {
                        throw new ServerErrorException("教务系统页面结构异常，未找到 __VIEWSTATE");
                    }
                    Element xndFormElement = document.getElementsByAttributeValue("name", "xnd").first();
                    if (xndFormElement == null) {
                        throw new ServerErrorException("教务系统页面结构异常，未找到学年下拉框");
                    }
                    Element xndFirstOption = xndFormElement.select("option").first();
                    if (xndFirstOption == null) {
                        throw new ServerErrorException("教务系统页面结构异常，学年下拉框无选项");
                    }
                    Element xqdFormElement = document.getElementsByAttributeValue("name", "xqd").first();
                    if (xqdFormElement == null) {
                        throw new ServerErrorException("教务系统页面结构异常，未找到学期下拉框");
                    }
                    Elements xqdFormOptions = xqdFormElement.select("option");
                    int termIndex = WeekUtils.getCurrentTerm() - 1;
                    if (termIndex < 0 || termIndex >= xqdFormOptions.size()) {
                        throw new ServerErrorException("教务系统页面结构异常，学期选项索引越界");
                    }
                    HttpPost httpPost = new HttpPost(JWGL_BASE + "/xskbcx.aspx?xh=" + credential.getNumber());
                    List<BasicNameValuePair> form = new ArrayList<>();
                    form.add(new BasicNameValuePair("__EVENTTARGET", ""));
                    form.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
                    form.add(new BasicNameValuePair("__VIEWSTATE", viewStateElement.val()));
                    form.add(new BasicNameValuePair("xnd", xndFirstOption.attr("value")));
                    form.add(new BasicNameValuePair("xqd", xqdFormOptions.get(termIndex).attr("value")));
                    httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
                    httpResponse = httpClient.execute(httpPost);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()).replace("<br>", "$info$"));
                    if (httpResponse.getStatusLine().getStatusCode() != 200) {
                        throw new ServerErrorException("教务系统异常");
                    }
                }
                return document;
            }
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                String location = httpResponse.getFirstHeader("Location") != null
                        ? httpResponse.getFirstHeader("Location").getValue() : "";
                if ("/loginTs/loginTs_yzsb.html".equals(location)) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                throw new PasswordIncorrectException("账号密码错误");
            }
            throw new ServerErrorException("教务系统异常");
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 空闲教室：时间戳校验 + 进入学生主页 + 打开空课室查询页（xxjsjy.aspx），返回初始页 Document，供 Service 解析表单并组 POST 参数。
     */
    public Document fetchSpareRoomInitialDocument(String sessionId, EduSessionCredential credential)
            throws IOException, TimeStampIncorrectException, PasswordIncorrectException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, EDU_REQUEST_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(JWGL_BASE + "/cas_verify.aspx?i=" + credential.getUsername()
                    + "&k=" + credential.getKeycode() + "&timestamp=" + credential.getTimestamp());
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (document.toString().equals("您登陆的系统已经很长时间没有操作了，为安全起见请重新登录后再进行操作！")) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                httpGet = new HttpGet(JWGL_BASE + "/xs_main.aspx?xh=" + credential.getNumber() + "&type=1");
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new ServerErrorException("教务系统异常");
                }
                httpGet = new HttpGet(JWGL_BASE + "/xxjsjy.aspx?xh=" + credential.getNumber());
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new ServerErrorException("教务系统异常");
                }
                return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            }
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                String location = httpResponse.getFirstHeader("Location") != null
                        ? httpResponse.getFirstHeader("Location").getValue() : "";
                if ("/loginTs/loginTs_yzsb.html".equals(location)) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                throw new PasswordIncorrectException("账号密码错误");
            }
            throw new ServerErrorException("教务系统异常");
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 空闲教室：提交查询表单（分页或首次查询），返回结果页 Document。调用前需已通过 fetchSpareRoomInitialDocument 拿到表单并完成 Cookie 同步。
     *
     * @param formAction Form1 的 action 相对路径（如 xxjsjy.aspx）
     * @param formParams 表单参数列表（__EVENTTARGET、__VIEWSTATE、xiaoq、jslb 等）
     */
    public Document submitSpareRoomForm(String sessionId, EduSessionCredential credential,
                                        String formAction, List<BasicNameValuePair> formParams)
            throws IOException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, EDU_REQUEST_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpPost httpPost = new HttpPost(JWGL_BASE + "/" + formAction);
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("教务系统异常");
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 教务学生主页：时间戳校验 + 进入 xs_main，返回学生主页 Document。供评教等仅需主页菜单的流程使用。
     */
    public Document fetchEduMainPage(String sessionId, EduSessionCredential credential)
            throws IOException, TimeStampIncorrectException, PasswordIncorrectException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, EDU_REQUEST_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(JWGL_BASE + "/cas_verify.aspx?i=" + credential.getUsername()
                    + "&k=" + credential.getKeycode() + "&timestamp=" + credential.getTimestamp());
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (document.toString().equals("您登陆的系统已经很长时间没有操作了，为安全起见请重新登录后再进行操作！")) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                httpGet = new HttpGet(JWGL_BASE + "/xs_main.aspx?xh=" + credential.getNumber());
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new ServerErrorException("教务系统异常");
                }
                return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            }
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                String location = httpResponse.getFirstHeader("Location") != null
                        ? httpResponse.getFirstHeader("Location").getValue() : "";
                if ("/loginTs/loginTs_yzsb.html".equals(location)) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                }
                throw new PasswordIncorrectException("账号密码错误");
            }
            throw new ServerErrorException("教务系统异常");
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 教务系统内 GET 任意相对路径（需已通过 fetchEduMainPage 等完成校验并同步 Cookie）。用于评教进入列表首项等。
     *
     * @param relativePath 相对路径，如 xs_jxpj.aspx?xh=xxx，可有或无前导 /
     */
    public Document fetchEduPage(String sessionId, EduSessionCredential credential, String relativePath)
            throws IOException, ServerErrorException {
        String path = relativePath != null && relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, EDU_REQUEST_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(JWGL_BASE + "/" + path);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("教务系统异常");
            }
            return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }

    /**
     * 教师课表：进入教师主页 + 课表查询页，可选按教师名、学年学期 POST 查询，返回课表页 Document。若页面为“请登录”，由调用方负责重新登录后重试。
     *
     * @param sessionId 会话 ID（已携带教师登录 Cookie 时使用）
     * @param teacherUsername 教师工号
     * @param teacherName 教师姓名（可为空，为空则不 POST）
     * @param year 学年（POST 时使用，可为空）
     * @param term 学期（POST 时使用，可为空）
     * @return 课表页 Jsoup Document（含 Table6）
     */
    public Document fetchTeacherScheduleDocument(String sessionId, String teacherUsername, String teacherName,
                                                 String year, String term)
            throws IOException, PasswordIncorrectException, ServerErrorException {
        HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, EDU_REQUEST_TIMEOUT_SEC);
        CloseableHttpClient httpClient = httpClientSession.getCloseableHttpClient();
        CookieStore cookieStore = httpClientSession.getCookieStore();
        try {
            HttpGet httpGet = new HttpGet(JWGL_BASE + "/js_main.aspx?xh=" + teacherUsername);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("教务系统异常");
            }
            if (document.title().equals("欢迎使用正方教务管理系统！请登录")) {
                throw new ServerErrorException("需要重新登录");
            }
            if (!document.title().equals("正方教务管理系统")) {
                throw new ServerErrorException("教务系统异常");
            }
            httpGet = new HttpGet(JWGL_BASE + "/jstjkbcx.aspx?zgh=" + teacherUsername);
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36");
            httpGet.setHeader("Referer", JWGL_BASE + "/js_main.aspx?xh=" + teacherUsername);
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new ServerErrorException("进入教师个人课表查询页面异常");
            }
            if (teacherName != null && !teacherName.trim().isEmpty()) {
                HttpPost httpPost = new HttpPost(JWGL_BASE + "/jstjkbcx.aspx?zgh=" + teacherUsername);
                httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36");
                httpPost.setHeader("Referer", JWGL_BASE + "/jstjkbcx.aspx?zgh=" + teacherUsername);
                List<BasicNameValuePair> form = new ArrayList<>();
                Elements eventTargetElements = document.getElementsByAttributeValue("name", "__EVENTTARGET");
                Elements eventArgumentElements = document.getElementsByAttributeValue("name", "__EVENTARGUMENT");
                Elements viewStateElements = document.getElementsByAttributeValue("name", "__VIEWSTATE");
                if (viewStateElements.isEmpty()) {
                    throw new ServerErrorException("教务系统页面结构异常，未找到 __VIEWSTATE");
                }
                form.add(new BasicNameValuePair("__EVENTTARGET", eventTargetElements.val()));
                form.add(new BasicNameValuePair("__EVENTARGUMENT", eventArgumentElements.val()));
                form.add(new BasicNameValuePair("__VIEWSTATE", viewStateElements.val()));
                form.add(new BasicNameValuePair("xn", year != null ? year : ""));
                form.add(new BasicNameValuePair("xq", term != null ? term : ""));
                form.add(new BasicNameValuePair("bm", ""));
                form.add(new BasicNameValuePair("TextBox1", teacherName.trim()));
                form.add(new BasicNameValuePair("Button2", "查询教师"));
                form.add(new BasicNameValuePair("js", teacherUsername));
                httpPost.setEntity(new UrlEncodedFormEntity(form, StandardCharsets.UTF_8));
                httpResponse = httpClient.execute(httpPost);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new ServerErrorException("提交课表查询请求异常");
                }
            }
            return document;
        } finally {
            if (httpClient != null) {
                try { httpClient.close(); } catch (IOException ignored) { }
            }
            if (cookieStore != null) {
                HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }
}
