package com.linguancheng.gdeiassistant.Service.ScheduleQuery;

import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import com.linguancheng.gdeiassistant.Tools.HttpClientUtils;
import com.linguancheng.gdeiassistant.Pojo.Entity.TeacherSchedule;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.UserLogin.TeacherLoginService;
import com.linguancheng.gdeiassistant.Tools.ScheduleUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeacherQueryService {

    private String url;

    @Autowired
    private TeacherLoginService teacherLoginService;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }

    private Log log = LogFactory.getLog(TeacherQueryService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.teacherquery']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
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
    public BaseResult<List<TeacherSchedule>, ServiceResultEnum> TeacherScheduleQuery(String sessionId
            , String username, String password, String year, String term, String teacherName) {
        BaseResult<List<TeacherSchedule>, ServiceResultEnum> result = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId
                    , true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            HttpGet httpGet = new HttpGet(url + "js_main.aspx?xh=" + username);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                boolean reLogin = false;
                if (document.title().equals("欢迎使用正方教务管理系统！请登录")) {
                    //登录凭证过期，重新登录
                    LoginResultEnum loginResultEnum = teacherLoginService
                            .TeacherLogin(sessionId, username, password);
                    switch (loginResultEnum) {
                        case TIME_OUT:
                            throw new NetWorkTimeoutException("登录教务系统超时");

                        case SERVER_ERROR:
                            throw new ServerErrorException("教务系统异常");

                        case PASSWORD_ERROR:
                            throw new PasswordIncorrectException("账号密码错误");

                        case LOGIN_SUCCESS:
                            reLogin = true;
                            break;
                    }
                }
                if (document.title().equals("正方教务管理系统") || reLogin) {
                    //进入教师个人课表查询页面
                    httpGet = new HttpGet(url + "jstjkbcx.aspx?zgh=" + username);
                    httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36");
                    httpGet.setHeader("Referer", url + "js_main.aspx?xh=" + username);
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        if (StringUtils.isNotBlank(teacherName)) {
                            HttpPost httpPost = new HttpPost("http://jwgl.gdei.edu.cn/jstjkbcx.aspx?zgh=" + username);
                            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36");
                            httpPost.setHeader("Referer", url + "jstjkbcx.aspx?zgh=" + username);
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
                                                String string[] = tds.get(currentColumnIndexInThisRow).text().split(" ");
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
                            result.setResultData(schedulesWithoutSpecialEmptySchedule);
                            result.setResultType(ServiceResultEnum.SUCCESS);
                            return result;
                        }
                        throw new ServerErrorException("提交课表查询请求异常");
                    }
                    throw new ServerErrorException("进入教师个人课表查询页面异常");
                }
                throw new ServerErrorException("教务系统异常");
            }
            throw new ServerErrorException("教务系统异常");
        } catch (IOException e) {
            log.error("教师个人课表查询异常：", e);
            result.setResultType(ServiceResultEnum.TIME_OUT);
        } catch (ServerErrorException e) {
            log.error("教师个人课表查询异常：", e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
        } catch (PasswordIncorrectException e) {
            log.error("教师个人课表查询异常：", e);
            result.setResultType(ServiceResultEnum.PASSWORD_INCORRECT);
        } catch (Exception e) {
            log.error("教师个人课表查询异常：", e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
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
        return result;
    }
}
