package cn.gdeiassistant.Service.AcademicAffairs.ScheduleQuery;

import cn.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Exception.CommonException.ServerErrorException;
import cn.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.Exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.Pojo.Document.CustomScheduleDocument;
import cn.gdeiassistant.Pojo.Document.ScheduleDocument;
import cn.gdeiassistant.Pojo.Entity.CustomSchedule;
import cn.gdeiassistant.Pojo.Entity.Schedule;
import cn.gdeiassistant.Pojo.Entity.TeacherSchedule;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import cn.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import cn.gdeiassistant.Pojo.UserLogin.UserCertificate;
import cn.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.TeacherLoginService;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.Utils.HttpClientUtils;
import cn.gdeiassistant.Tools.Utils.ScheduleUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import cn.gdeiassistant.Tools.Utils.WeekUtils;
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
import java.util.List;

@Service
public class ScheduleService {

    private final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private TeacherLoginService teacherLoginService;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private ScheduleDao scheduleDao;

    /**
     * 获取自定义课表信息
     *
     * @param sessionId
     * @return
     */
    public CustomScheduleDocument GetCustomSchedule(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        return scheduleDao.queryCustomSchedule(user.getUsername());
    }

    /**
     * 添加自定义课程信息
     *
     * @param sessionId
     * @param customSchedule
     * @throws GenerateScheduleException
     * @throws CountOverLimitException
     */
    public void AddCustomSchedule(String sessionId, CustomSchedule customSchedule)
            throws GenerateScheduleException, CountOverLimitException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        scheduleDao.addCustomSchedule(user.getUsername(), customSchedule);
    }

    /**
     * 删除自定义课程信息
     *
     * @param sessionId
     * @param id
     */
    public void DeleteCustomSchedule(String sessionId, String id) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        scheduleDao.deleteCustomSchedule(user.getUsername(), id);
    }

    /**
     * 清空缓存的课表信息
     *
     * @param sessionId
     */
    public void ClearSchedule(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        scheduleDao.removeSchedule(user.getUsername());
    }

    /**
     * 查询课表
     *
     * @param sessionId
     * @param queryWeek
     * @return
     * @throws Exception
     */
    public ScheduleQueryResult QuerySchedule(String sessionId, Integer queryWeek) throws NetWorkTimeoutException, PasswordIncorrectException, ServerErrorException, TimeStampIncorrectException {
        ScheduleQueryResult scheduleQueryResult = new ScheduleQueryResult();
        //优先从缓存中加载
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        ScheduleDocument scheduleDocument = scheduleDao.querySchedule(user.getUsername());
        if (scheduleDocument == null) {
            UserCertificate userCertificate = userCertificateService.GetUserSessionCertificate(sessionId);
            //从教务系统实时获取
            CloseableHttpClient httpClient = null;
            CookieStore cookieStore = null;
            try {
                HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, 15);
                httpClient = httpClientSession.getCloseableHttpClient();
                cookieStore = httpClientSession.getCookieStore();
                HttpGet httpGet = new HttpGet("http://jwgl.gdei.edu.cn/cas_verify.aspx?i="
                        + userCertificate.getUser().getUsername() + "&k="
                        + userCertificate.getKeycode() + "&timestamp=" + userCertificate.getTimestamp());
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (document.toString().equals("您登陆的系统已经很长时间没有操作了，为安全起见请重新登录后再进行操作！")) {
                        throw new TimeStampIncorrectException("时间戳校验失败");
                    } else {
                        //进入教务系统个人主页
                        httpGet = new HttpGet("http://jwgl.gdei.edu.cn/xs_main.aspx?xh="
                                + userCertificate.getNumber() + "&type=1");
                        httpResponse = httpClient.execute(httpGet);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            //成功进入学生个人主页,进行课表查询操作
                            httpGet = new HttpGet("http://jwgl.gdei.edu.cn/xskbcx.aspx?xh="
                                    + userCertificate.getNumber());
                            httpResponse = httpClient.execute(httpGet);
                            //将<br>标签替换为转义标签以便解析处理
                            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()).replace("<br>", "$info$"));
                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                Elements xqdOptions = document.getElementsByAttributeValue("name", "xqd").first().select("option");
                                //默认的学年学期是否与配置文件中配置的学年学期相同
                                boolean isDefaultEqualsCurrent = false;
                                int defaultTerm = 0;
                                int defaultYear = 0;
                                for (Element option : xqdOptions) {
                                    if (option.hasAttr("selected")) {
                                        defaultTerm = Integer.valueOf(option.attr("value"));
                                    }
                                }
                                Elements xndOptions = document.getElementsByAttributeValue("name", "xnd").first().select("option");
                                for (Element option : xndOptions) {
                                    if (option.hasAttr("selected")) {
                                        defaultYear = Integer.parseInt((option.attr("value").split("-"))[0]);
                                    }
                                }
                                if (defaultYear == WeekUtils.GetCurrentYear() && defaultTerm == WeekUtils.GetCurrentTerm()) {
                                    isDefaultEqualsCurrent = true;
                                }
                                if (!isDefaultEqualsCurrent) {
                                    HttpPost httpPost = new HttpPost("http://jwgl.gdei.edu.cn/xskbcx.aspx?xh="
                                            + userCertificate.getNumber());
                                    List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                                    basicNameValuePairList.add(new BasicNameValuePair("__EVENTTARGET", ""));
                                    basicNameValuePairList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
                                    basicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", document.getElementsByAttributeValue("name", "__VIEWSTATE").first().val()));
                                    basicNameValuePairList.add(new BasicNameValuePair("xnd", document.getElementsByAttributeValue("name", "xnd").first().select("option").first().attr("value")));
                                    basicNameValuePairList.add(new BasicNameValuePair("xqd", document.getElementsByAttributeValue("name", "xqd").first().select("option").get(WeekUtils.GetCurrentTerm() - 1).attr("value")));
                                    httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                                    httpResponse = httpClient.execute(httpPost);
                                    //将<br>标签替换为<info>转义标签以便解析处理
                                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()).replace("<br>", "$info$"));
                                    if (httpResponse.getStatusLine().getStatusCode() != 200) {
                                        throw new ServerErrorException("教务系统异常");
                                    }
                                }
                                //获取存放课表信息的表格
                                Element table = document.getElementById("Table1");
                                //获取表格中的所有行
                                Elements trs = table.select("tr");
                                //特殊的空课程,用于标记当前单元格被上边的课程信息占用,即上边同列课程的课程时长大于1
                                Schedule specialEmptySchedule = new Schedule();
                                //存放课表信息的数组,包含特殊的空课程对象
                                Schedule[][] schedulesWithSpecialEmptySchedule = new Schedule[70][10];
                                //存放课表信息的列表,过滤特殊的空课程对象后添加入该列表
                                List<Schedule> schedulesWithoutSpecialEmptySchedule = new ArrayList<>();
                                //当前单元格位置position值
                                int currentPosition = 0;
                                //当前行最大位置position值
                                int currentRowMaxPosition = 0;
                                //前两行为行列信息,从第三行开始获取信息
                                for (int row = 2; row < trs.size(); row++) {
                                    //获取该行内所有列
                                    Elements tds = trs.get(row).select("td");
                                    //记录当前访问列数据的游标,若当前为第3行或第7行或第11行,初始列游标值为2,否则初始值为1
                                    //因为特殊行里面的第一列包含上午/下午的信息提示
                                    int currentColumnIndexInThisRow;
                                    if (row == 2 || row == 6 || row == 10) {
                                        currentColumnIndexInThisRow = 2;
                                    } else {
                                        currentColumnIndexInThisRow = 1;
                                    }
                                    //当前行的列最大游标
                                    int maxColumnIndex;
                                    if (tds.size() < 7) {
                                        maxColumnIndex = tds.size() - 1;
                                    } else {
                                        maxColumnIndex = 6;
                                    }
                                    //遍历当前行,获取课表信息
                                    for (currentRowMaxPosition = currentRowMaxPosition + 7; currentPosition < currentRowMaxPosition; currentPosition++) {
                                        if (schedulesWithSpecialEmptySchedule[currentPosition][0] == specialEmptySchedule) {
                                            //当前position指向特殊空Schedule对象,跳过当前单元格
                                        } else {
                                            //判断当前行是否已经遍历完
                                            if (currentPosition % 7 <= maxColumnIndex) {
                                                //判断当前position的课程信息是否为空课表信息
                                                //下面的字符非空格而是一个特殊的Unicode字符
                                                if (tds.get(currentColumnIndexInThisRow).text().equals(" ")) {
                                                    //不存在课表信息
                                                } else {
                                                    //当前td标签属性,代表课程时长
                                                    int rowspan;
                                                    //判断td标签有无rowspan属性,该属性代表课程时长
                                                    if (tds.get(currentColumnIndexInThisRow).hasAttr("rowspan")) {
                                                        //通过rowspan属性得到课程时长
                                                        rowspan = Integer.parseInt(tds.get(currentColumnIndexInThisRow).attr("rowspan"));
                                                        //将当前单元格下方对应的原单元格的课程信息标记为特殊空课程
                                                        switch (rowspan) {
                                                            case 2:
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 7][0] = specialEmptySchedule;
                                                                break;

                                                            case 3:
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 7][0] = specialEmptySchedule;
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 14][0] = specialEmptySchedule;
                                                                break;

                                                            case 4:
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 7][0] = specialEmptySchedule;
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 14][0] = specialEmptySchedule;
                                                                schedulesWithSpecialEmptySchedule[currentPosition + 21][0] = specialEmptySchedule;
                                                                break;
                                                        }
                                                    } else {
                                                        //如果td标签没有rowspan属性,则该课程课程时长为默认的1
                                                        rowspan = 1;
                                                    }
                                                    //将单元格里的课表信息按独立行进行分割单独处理
                                                    String[] string = tds.get(currentColumnIndexInThisRow).text().split("\\$info\\$");
                                                    //记录单元格中的独立课表信息下标
                                                    int n = 0;
                                                    if (string.length == 0) {
                                                        schedulesWithSpecialEmptySchedule[currentPosition][0] = specialEmptySchedule;
                                                    } else {
                                                        for (int j = 0; j < string.length; j++) {
                                                            if (string[j].isEmpty() || string[j].substring(0, 1).equals("<") || string[j].equals(" ") || string[j].substring(0, 1).equals("(")) {
                                                                //不是有效的课表头信息,跳过并查询下一个独立行的信息,直到得到有效的课表头信息
                                                            } else {
                                                                //有效的课表头信息,进行信息处理
                                                                String[] time = string[j + 2].split("[{]");
                                                                //课程周数
                                                                String week = null;
                                                                if (time.length < 2) {
                                                                    week = time[0].substring(0, time[0].length() - 1);
                                                                } else {
                                                                    week = time[1].substring(0, time[1].length() - 1);
                                                                }
                                                                //课程名称
                                                                String name = string[j];
                                                                //课程节数
                                                                String lesson = time[0];
                                                                //课程类型
                                                                String type = string[j + 1];
                                                                //任课教师
                                                                String teacher = string[j + 3];
                                                                //上课地点
                                                                String location = "";
                                                                //检查是否已经安排课程
                                                                if (string.length <= j + 4 || string[j + 4] == null) {
                                                                    location = "暂未安排";
                                                                } else {
                                                                    if (string[j + 4].contains("（")) {
                                                                        String[] locations = string[j + 4].split("（");
                                                                        location = locations[0];
                                                                    } else {
                                                                        location = string[j + 4];
                                                                    }
                                                                }
                                                                Schedule schedule = new Schedule();
                                                                schedule.setPosition(currentPosition);
                                                                schedule.setScheduleLength(rowspan);
                                                                schedule.setScheduleName(name);
                                                                schedule.setScheduleType(type);
                                                                schedule.setScheduleLesson(lesson);
                                                                String[] weekString = week.split("-");
                                                                int minWeekNumber;
                                                                int maxWeekNumber;
                                                                if (weekString[1].contains("|")) {
                                                                    String[] s1 = weekString[1].split("\\|");
                                                                    minWeekNumber = Integer.valueOf(weekString[0].substring(1));
                                                                    maxWeekNumber = Integer.valueOf(s1[0].substring(0, s1[0].length() - 1));
                                                                } else {
                                                                    minWeekNumber = Integer.valueOf(weekString[0].substring(1));
                                                                    maxWeekNumber = Integer.valueOf(weekString[1].substring(0, weekString[1].length() - 1));
                                                                }
                                                                schedule.setMinScheduleWeek(minWeekNumber);
                                                                schedule.setMaxScheduleWeek(maxWeekNumber);
                                                                schedule.setScheduleWeek("第" + minWeekNumber + "周至第" + maxWeekNumber + "周");
                                                                schedule.setScheduleTeacher(teacher);
                                                                schedule.setScheduleLocation(location);
                                                                schedule.setRow(row - 2);
                                                                if (row == 2 || row == 6 || row == 10) {
                                                                    schedule.setColumn(currentColumnIndexInThisRow - 2);
                                                                } else {
                                                                    schedule.setColumn(currentColumnIndexInThisRow - 1);
                                                                }
                                                                schedule.setColorCode(ScheduleUtils.getScheduleColor(currentPosition));
                                                                schedulesWithSpecialEmptySchedule[currentPosition][n] = schedule;
                                                                n++;
                                                                j = j + 4;
                                                            }
                                                        }
                                                    }
                                                }
                                                currentColumnIndexInThisRow = currentColumnIndexInThisRow + 1;
                                            }
                                        }
                                    }
                                }
                                for (Schedule[] schedules : schedulesWithSpecialEmptySchedule) {
                                    for (Schedule schedule : schedules) {
                                        if (schedule != null && schedule != specialEmptySchedule) {
                                            schedulesWithoutSpecialEmptySchedule.add(schedule);
                                        }
                                    }
                                }
                                scheduleQueryResult.setScheduleList(schedulesWithoutSpecialEmptySchedule);
                            } else if (httpResponse.getStatusLine().getStatusCode() == 302) {
                                throw new PasswordIncorrectException("账号密码错误");
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
            } catch (ServerErrorException e) {
                logger.error("查询课表异常：", e);
                throw new ServerErrorException("教务系统异常");
            } catch (PasswordIncorrectException ignored) {
                throw new PasswordIncorrectException("用户密码错误");
            } catch (TimeStampIncorrectException e) {
                logger.error("查询课表异常；", e);
                throw new TimeStampIncorrectException("时间戳校验失败");
            } catch (IOException e) {
                logger.error("查询课表异常：", e);
                throw new NetWorkTimeoutException("网络连接超时");
            } catch (Exception e) {
                logger.error("查询课表异常：", e);
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
        } else {
            //从缓存中获取
            scheduleQueryResult.setScheduleList(scheduleQueryResult.getScheduleList());
        }
        //获取自定义课表信息
        CustomScheduleDocument customScheduleDocument = GetCustomSchedule(sessionId);
        scheduleQueryResult.getScheduleList().addAll(customScheduleDocument.getScheduleMap().values());
        if (queryWeek == null) {
            //无指定查询周数，则默认返回当前周数课表
            scheduleQueryResult.setScheduleList(ScheduleUtils.GetSpecifiedWeekSchedule
                    (scheduleQueryResult.getScheduleList(), WeekUtils.GetCurrentWeek()));
            scheduleQueryResult.setWeek(WeekUtils.GetCurrentWeek());
        } else if (queryWeek.equals(0)) {
            //若周数指定为0，则返回所有周数的课表
            scheduleQueryResult.setScheduleList(scheduleQueryResult.getScheduleList());
            scheduleQueryResult.setWeek(0);
        } else {
            //返回指定周数的课表
            scheduleQueryResult.setScheduleList(ScheduleUtils.GetSpecifiedWeekSchedule
                    (scheduleQueryResult.getScheduleList(), queryWeek));
            scheduleQueryResult.setWeek(queryWeek);
        }
        return scheduleQueryResult;
    }

    /**
     * 查询教师个人课表
     *
     * @param sessionId
     * @param username
     * @param password
     * @param year
     * @param term
     * @param teacherName
     * @return
     */
    @Deprecated
    public List<TeacherSchedule> TeacherScheduleQuery(String sessionId
            , String username, String password, String year, String term, String teacherName) throws NetWorkTimeoutException, ServerErrorException, PasswordIncorrectException {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, 15);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            HttpGet httpGet = new HttpGet("http://jwgl.gdei.edu.cn/js_main.aspx?xh=" + username);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                boolean reLogin = false;
                if (document.title().equals("欢迎使用正方教务管理系统！请登录")) {
                    //登录凭证过期，重新登录
                    teacherLoginService.TeacherLogin(sessionId, username, password);
                    reLogin = true;
                }
                if (document.title().equals("正方教务管理系统") || reLogin) {
                    //进入教师个人课表查询页面
                    httpGet = new HttpGet("http://jwgl.gdei.edu.cn/jstjkbcx.aspx?zgh=" + username);
                    httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36");
                    httpGet.setHeader("Referer", "http://jwgl.gdei.edu.cn/js_main.aspx?xh=" + username);
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        if (StringUtils.isNotBlank(teacherName)) {
                            HttpPost httpPost = new HttpPost("http://jwgl.gdei.edu.cn/jstjkbcx.aspx?zgh=" + username);
                            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36");
                            httpPost.setHeader("Referer", "http://jwgl.gdei.edu.cn/jstjkbcx.aspx?zgh=" + username);
                            List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
                            basicNameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", document.getElementsByAttributeValue("name", "__EVENTTARGET").val()));
                            basicNameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", document.getElementsByAttributeValue("name", "__EVENTARGUMENT").val()));
                            basicNameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", document.getElementsByAttributeValue("name", "__VIEWSTATE").val()));
                            basicNameValuePairs.add(new BasicNameValuePair("xn", year));
                            basicNameValuePairs.add(new BasicNameValuePair("xq", term));
                            basicNameValuePairs.add(new BasicNameValuePair("bm", ""));
                            basicNameValuePairs.add(new BasicNameValuePair("TextBox1", teacherName));
                            basicNameValuePairs.add(new BasicNameValuePair("Button2", "查询教师"));
                            basicNameValuePairs.add(new BasicNameValuePair("js", username));
                            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
                            httpResponse = httpClient.execute(httpPost);
                            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        }
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            //获取存放课表信息的表格
                            Element table = document.getElementById("Table6");
                            //获取表格中的所有行
                            Elements trs = table.select("tr");
                            //特殊的空课程,用于标记当前单元格被上边的课程信息占用,即上边同列课程的课程时长大于1
                            TeacherSchedule specialEmptySchedule = new TeacherSchedule();
                            //存放课表信息的数组,包含特殊的空课程对象
                            TeacherSchedule[] schedulesWithSpecialEmptySchedule = new TeacherSchedule[84];
                            //存放课表信息的列表,过滤特殊的空课程对象后添加入该列表
                            List<TeacherSchedule> schedulesWithoutSpecialEmptySchedule = new ArrayList<>();
                            //当前单元格位置position值
                            int currentPosition = 0;
                            //当前行最大位置position值
                            int currentRowMaxPosition = 0;
                            //前两行为行列信息,从第三行开始获取信息
                            for (int row = 2; row < trs.size(); row++) {
                                //获取该行内所有列
                                Elements tds = trs.get(row).select("td");
                                //记录当前访问列数据的游标,若当前为第3行或第8行或第13行,初始列游标值为2,否则初始值为1
                                //因为特殊行里面的第一列包含上午/下午的信息提示
                                int currentColumnIndexInThisRow;
                                if (row == 2 || row == 7 || row == 11) {
                                    currentColumnIndexInThisRow = 2;
                                } else {
                                    currentColumnIndexInThisRow = 1;
                                }
                                //当前行的列最大游标
                                int maxColumnIndex;
                                if (tds.size() < 7) {
                                    maxColumnIndex = tds.size() - 1;
                                } else {
                                    maxColumnIndex = 6;
                                }
                                //遍历当前行,获取课表信息
                                for (currentRowMaxPosition = currentRowMaxPosition + 7; currentPosition < currentRowMaxPosition; currentPosition++) {
                                    if (schedulesWithSpecialEmptySchedule[currentPosition] == specialEmptySchedule) {
                                        //当前position指向特殊空Schedule对象,跳过当前单元格
                                    } else {
                                        //判断当前行是否已经遍历完
                                        if (currentPosition % 7 <= maxColumnIndex) {
                                            //判断当前position的课程信息是否为空课表信息
                                            //下面的字符非空格而是一个特殊的Unicode字符
                                            if (tds.get(currentColumnIndexInThisRow).text().equals(" ")) {
                                                //不存在课表信息
                                            } else {
                                                //当前td标签属性,代表课程时长
                                                int rowspan;
                                                //判断td标签有无rowspan属性,该属性代表课程时长
                                                if (tds.get(currentColumnIndexInThisRow).hasAttr("rowspan")) {
                                                    //通过rowspan属性得到课程时长
                                                    rowspan = Integer.parseInt(tds.get(currentColumnIndexInThisRow).attr("rowspan"));
                                                    //将当前单元格下方对应的原单元格的课程信息标记为特殊空课程
                                                    switch (rowspan) {
                                                        case 2:
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 7] = specialEmptySchedule;
                                                            break;

                                                        case 3:
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 7] = specialEmptySchedule;
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 14] = specialEmptySchedule;
                                                            break;

                                                        case 4:
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 7] = specialEmptySchedule;
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 14] = specialEmptySchedule;
                                                            schedulesWithSpecialEmptySchedule[currentPosition + 21] = specialEmptySchedule;
                                                            break;
                                                    }
                                                } else {
                                                    //如果td标签没有rowspan属性,则该课程课程时长为默认的1
                                                    rowspan = 1;
                                                }
                                                //将单元格里的课表信息按独立行进行分割单独处理
                                                String[] string = tds.get(currentColumnIndexInThisRow).text().split(" ");
                                                //记录单元格中的独立课表信息下标
                                                for (int j = 0; j < string.length; j++) {
                                                    if (string.length == 0) {
                                                        schedulesWithSpecialEmptySchedule[currentPosition] = specialEmptySchedule;
                                                    } else if (string[j].isEmpty() || string[j].substring(0, 1).equals("<") || string[j].equals(" ") || string[j].substring(0, 1).equals("(")) {
                                                        //不是有效的课表头信息,跳过并查询下一个独立行的信息,直到得到有效的课表头信息
                                                    } else {
                                                        //有效的课表头信息,进行信息处理
                                                        String scheduleName = string[j];
                                                        String scheduleType = string[j + 1];
                                                        String scheduleTime = string[j + 2];
                                                        String scheduleWeek = (scheduleTime.split("\\("))[0];
                                                        String scheduleLesson = ((scheduleTime.split("\\("))[1]).split("\\)")[0];
                                                        String scheduleLocation = string[j + 4];
                                                        String scheduleClass = string[j + 5];
                                                        TeacherSchedule teacherSchedule = new TeacherSchedule();
                                                        teacherSchedule.setPosition(currentPosition);
                                                        teacherSchedule.setScheduleLength(rowspan);
                                                        teacherSchedule.setScheduleName(scheduleName);
                                                        teacherSchedule.setScheduleType(scheduleType);
                                                        teacherSchedule.setScheduleLesson(scheduleLesson);
                                                        teacherSchedule.setScheduleWeek(scheduleWeek);
                                                        teacherSchedule.setScheduleClass(scheduleClass);
                                                        teacherSchedule.setScheduleLocation(scheduleLocation);
                                                        teacherSchedule.setRow(row - 2);
                                                        if (row == 2 || row == 7 || row == 11) {
                                                            teacherSchedule.setColumn(currentColumnIndexInThisRow - 2);
                                                        } else {
                                                            teacherSchedule.setColumn(currentColumnIndexInThisRow - 1);
                                                        }
                                                        teacherSchedule.setColorCode(ScheduleUtils.getScheduleColor(currentPosition));
                                                        schedulesWithSpecialEmptySchedule[currentPosition] = teacherSchedule;
                                                        j = j + 5;
                                                    }
                                                }
                                            }
                                            currentColumnIndexInThisRow = currentColumnIndexInThisRow + 1;
                                        }
                                    }
                                }
                            }
                            for (TeacherSchedule teacherSchedule : schedulesWithSpecialEmptySchedule) {
                                if (teacherSchedule != null && teacherSchedule != specialEmptySchedule) {
                                    schedulesWithoutSpecialEmptySchedule.add(teacherSchedule);
                                }
                            }
                            return schedulesWithoutSpecialEmptySchedule;
                        }
                        throw new ServerErrorException("提交课表查询请求异常");
                    }
                    throw new ServerErrorException("进入教师个人课表查询页面异常");
                }
                throw new ServerErrorException("教务系统异常");
            }
            throw new ServerErrorException("教务系统异常");
        } catch (IOException e) {
            logger.error("教师个人课表查询异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (ServerErrorException e) {
            logger.error("教师个人课表查询异常：", e);
            throw new ServerErrorException("教务系统异常");
        } catch (PasswordIncorrectException ignored) {
            throw new PasswordIncorrectException("用户账号密码错误");
        } catch (Exception e) {
            logger.error("教师个人课表查询异常：", e);
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
}
