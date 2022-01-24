package cn.gdeiassistant.Service.GradeQuery;

import cn.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Exception.CommonException.ServerErrorException;
import cn.gdeiassistant.Exception.DatasourceException.MongodbNotConfiguredException;
import cn.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.Exception.QueryException.NotAvailableConditionException;
import cn.gdeiassistant.Exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.Pojo.Document.GradeDocument;
import cn.gdeiassistant.Pojo.Entity.Grade;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.GradeQuery.GradeCacheResult;
import cn.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import cn.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import cn.gdeiassistant.Pojo.UserLogin.UserCertificate;
import cn.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import cn.gdeiassistant.Repository.Redis.UserCertificate.UserCertificateDao;
import cn.gdeiassistant.Service.UserLogin.UserLoginService;
import cn.gdeiassistant.Tools.Utils.HttpClientUtils;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Service
public class GradeService {

    private Logger logger = LoggerFactory.getLogger(GradeService.class);

    private String url;

    private int timeout;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserCertificateDao userCertificateDao;

    @Autowired
    private GradeDao gradeDao;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }

    @Value("#{propertiesReader['timeout.gradequery']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    /**
     * 查询学科成绩
     *
     * @param sessionId
     * @param username
     * @param keycode
     * @param number
     * @param year
     * @return
     */
    private GradeQueryResult GradeQuery(String sessionId, String username, String keycode
            , String number, Long timestamp, int year) throws Exception {
        GradeQueryResult gradeQueryResult = new GradeQueryResult();
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, false, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //快速连接教务系统
            HttpGet httpGet = new HttpGet(url + "cas_verify.aspx?i=" + username + "&k="
                    + keycode + "&timestamp=" + timestamp);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (new String(document.toString().getBytes(StandardCharsets.UTF_8)
                        , StandardCharsets.UTF_8).equals("您登陆的系统已经很长时间没有操作了" +
                        "，为安全起见请重新登录后再进行操作！")) {
                    throw new TimeStampIncorrectException("时间戳校验失败");
                } else {
                    httpGet = new HttpGet(url + "xs_main.aspx?xh=" + number + "&type=1");
                    httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //成功进入学生个人主页,进行成绩查询操作
                        httpGet = new HttpGet(url + "xscj_gc.aspx?xh=" + number);
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            //获取所有可用的学年列表
                            List<String> yearList = new ArrayList<>();
                            Element yearSelect = document.getElementById("ddlXN");
                            Elements yearOptions = yearSelect.select("option");
                            for (Element element : yearOptions) {
                                if (!element.text().trim().isEmpty()) {
                                    yearList.add(element.text());
                                }
                            }
                            Collections.reverse(yearList);
                            //检查需要查询的学年是否可用
                            if (yearList.isEmpty() || yearList.size() <= year || year < -1) {
                                throw new NotAvailableConditionException("当前学年暂不可查询");
                            }
                            if (year == -1) {
                                //若没有指定查询的学年，则进行默认学年查询
                                year = yearList.size() - 1;
                            }
                            gradeQueryResult.setYear(year);
                            //封装查询成绩发送的数据
                            List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
                            basicNameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", document.select("input[name=__VIEWSTATE]").val()));
                            basicNameValuePairs.add(new BasicNameValuePair("Button5", "按学年查询"));
                            basicNameValuePairs.add(new BasicNameValuePair("ddlXN", yearList.get(year)));
                            basicNameValuePairs.add(new BasicNameValuePair("ddlXQ", ""));
                            HttpPost httpPost = new HttpPost(url + "xscj_gc.aspx?xh=" + number);
                            httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
                            httpResponse = httpClient.execute(httpPost);
                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                                //获取存放成绩的表格
                                Element element = document.getElementsByClass("datelist").first();
                                //得到所有的行
                                Elements trs = element.getElementsByTag("tr");
                                List<Grade> firstTermGradeList = new ArrayList<>();
                                List<Grade> secondTermGradeList = new ArrayList<>();
                                double firstTermIGP = 0;
                                double secondTermIGP = 0;
                                double firstCreditSum = 0;
                                double secondCreditSum = 0;
                                double firstTermGPA = 0;
                                double secondTermGPA = 0;
                                for (int i = 1; i < trs.size(); i++) {
                                    //首行是列名,从第二行开始
                                    Element e = trs.get(i);
                                    //得到行中的所有列
                                    Elements tds = e.getElementsByTag("td");
                                    String grade_year = tds.get(0).text();
                                    String grade_term = tds.get(1).text();
                                    String grade_id = tds.get(2).text();
                                    String grade_name = tds.get(3).text();
                                    String grade_type = tds.get(4).text();
                                    String grade_credit = tds.get(6).text();
                                    String grade_gpa = tds.get(7).text();
                                    String grade_score = tds.get(8).text();
                                    Grade grade = new Grade();
                                    grade.setGradeYear(grade_year);
                                    grade.setGradeTerm(grade_term);
                                    grade.setGradeId(grade_id);
                                    grade.setGradeName(grade_name);
                                    grade.setGradeType(grade_type);
                                    grade.setGradeCredit(grade_credit);
                                    grade.setGradeGpa(grade_gpa);
                                    grade.setGradeScore(grade_score);
                                    if (grade_term.equals("1")) {
                                        //计算第一学期IGP
                                        firstTermIGP = firstTermIGP + (Double.parseDouble(grade_credit) * Double.parseDouble(grade_gpa));
                                        firstCreditSum = firstCreditSum + Double.parseDouble(grade_credit);
                                        firstTermGradeList.add(grade);
                                    } else if (grade_term.equals("2")) {
                                        //计算第二学期IGP
                                        secondTermIGP = secondTermIGP + (Double.parseDouble(grade_credit) * Double.parseDouble(grade_gpa));
                                        secondCreditSum = secondCreditSum + Double.parseDouble(grade_credit);
                                        secondTermGradeList.add(grade);
                                    }
                                }
                                //计算第一学期GPA
                                if (firstCreditSum != 0) {
                                    firstTermGPA = firstTermIGP / firstCreditSum;
                                }
                                //计算第二学期GPA
                                if (secondCreditSum != 0) {
                                    secondTermGPA = secondTermIGP / secondCreditSum;
                                }
                                //保留两位小数
                                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                                firstTermIGP = Double.parseDouble(decimalFormat.format(firstTermIGP));
                                secondTermIGP = Double.parseDouble(decimalFormat.format(secondTermIGP));
                                firstTermGPA = Double.parseDouble(decimalFormat.format(firstTermGPA));
                                secondTermGPA = Double.parseDouble(decimalFormat.format(secondTermGPA));
                                gradeQueryResult.setFirstTermIGP(firstTermIGP);
                                gradeQueryResult.setFirstTermGPA(firstTermGPA);
                                gradeQueryResult.setSecondTermIGP(secondTermIGP);
                                gradeQueryResult.setSecondTermGPA(secondTermGPA);
                                gradeQueryResult.setFirstTermGradeList(firstTermGradeList);
                                gradeQueryResult.setSecondTermGradeList(secondTermGradeList);
                                return gradeQueryResult;
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
        } catch (PasswordIncorrectException ignored) {
            throw new PasswordIncorrectException("用户密码错误");
        } catch (TimeStampIncorrectException e) {
            logger.error("课程成绩查询异常：", e);
            throw new TimeStampIncorrectException("时间戳校验失败");
        } catch (NotAvailableConditionException e) {
            logger.error("课程成绩查询异常：", e);
            throw new NotAvailableConditionException("查询条件不可用");
        } catch (ServerErrorException e) {
            logger.error("课程成绩查询异常：", e);
            throw new ServerErrorException("教务系统异常");
        } catch (IOException e) {
            logger.error("课程成绩查询异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("课程成绩查询异常：", e);
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
     * 从MongoDB中获取用户缓存的成绩信息
     *
     * @param username
     * @param year
     * @return
     */
    public GradeQueryResult QueryUserGradeFromDocument(String username, Integer year) throws Exception {
        GradeQueryResult gradeQueryResult = new GradeQueryResult();
        GradeDocument gradeDocument = ReadGrade(username);
        if (gradeDocument != null) {
            List<List<Grade>> gradeLists = gradeDocument.getGradeList();
            if (year == null) {
                year = gradeLists.size() - 1;
            }
            if (gradeLists.size() == 0) {
                return null;
            }
            if (year >= gradeLists.size()) {
                throw new ErrorQueryConditionException("查询条件不可用");
            }
            List<Grade> gradeList = gradeDocument.getGradeList().get(year);
            List<Grade> firstTermGradeList = new ArrayList<>();
            List<Grade> secondTermGradeList = new ArrayList<>();
            for (Grade grade : gradeList) {
                if (grade.getGradeTerm().equals("1")) {
                    firstTermGradeList.add(grade);
                } else {
                    secondTermGradeList.add(grade);
                }
            }
            Double firstTermGPA = gradeDocument.getFirstTermGPAList().get(year);
            Double secondTermGPA = gradeDocument.getSecondTermGPAList().get(year);
            Double firstTermIGP = gradeDocument.getFirstTermIGPList().get(year);
            Double secondTermIGP = gradeDocument.getSecondTermIGPList().get(year);
            gradeQueryResult.setFirstTermGPA(firstTermGPA);
            gradeQueryResult.setFirstTermIGP(firstTermIGP);
            gradeQueryResult.setSecondTermGPA(secondTermGPA);
            gradeQueryResult.setSecondTermIGP(secondTermIGP);
            gradeQueryResult.setFirstTermGradeList(firstTermGradeList);
            gradeQueryResult.setSecondTermGradeList(secondTermGradeList);
            gradeQueryResult.setYear(year);
            return gradeQueryResult;
        }
        return null;
    }

    /**
     * 从教务系统获取用户的成绩信息
     *
     * @param sessionId
     * @param user
     * @param year
     * @return
     */
    public GradeQueryResult QueryGradeFromSystem(String sessionId, User user, Integer year) throws Exception {
        if (year == null) {
            //若没有指定查询的学年，则进行默认学年查询
            year = -1;
        }
        //检测是否已与教务系统进行会话同步
        UserCertificate userCertificate = userCertificateDao.queryUserCertificate(user.getUsername());
        if (userCertificate == null) {
            //进行会话同步
            userCertificate = userLoginService.SyncUpdateSession(sessionId, user);
        }
        return GradeQuery(sessionId, user.getUsername()
                , userCertificate.getKeycode(), userCertificate.getNumber()
                , userCertificate.getTimestamp(), year);
    }

    /**
     * 优先从缓存中获取成绩信息，若缓存不存在，则通过教务系统获取
     *
     * @param sessionId
     * @param user
     * @param year
     * @return
     * @throws Exception
     */
    public GradeQueryResult QueryGrade(String sessionId, User user, Integer year) throws Exception {
        try {
            GradeQueryResult gradeQueryResult = QueryUserGradeFromDocument(user.getUsername(), year);
            if (gradeQueryResult == null) {
                return QueryGradeFromSystem(sessionId, user, year);
            }
            return gradeQueryResult;
        } catch (Exception e) {
            return QueryGradeFromSystem(sessionId, user, year);
        }
    }

    /**
     * 查询用户保存的成绩信息
     *
     * @param username
     * @return
     */
    public GradeDocument ReadGrade(String username) {
        return gradeDao.queryGradeByUsername(username);
    }

    /**
     * 清空用户保存的成绩信息
     *
     * @param username
     */
    public void ClearGrade(String username) throws MongodbNotConfiguredException {
        gradeDao.removeGrade(username);
    }

    /**
     * 异步获取教务系统成绩信息
     *
     * @param semaphore
     * @param user
     * @return
     */
    @Async
    public ListenableFuture<GradeCacheResult> QueryGradeData(Semaphore semaphore, User user) {
        try {
            semaphore.acquire();
            GradeCacheResult gradeCacheResult = new GradeCacheResult();
            gradeCacheResult.setFirstTermGPAArray(new Double[4]);
            gradeCacheResult.setSecondTermGPAArray(new Double[4]);
            gradeCacheResult.setFirstTermIGPArray(new Double[4]);
            gradeCacheResult.setSecondTermIGPArray(new Double[4]);
            gradeCacheResult.setGradeListArray(new ArrayList[4]);
            CountDownLatch countDownLatch = new CountDownLatch(4);
            for (int i = 0; i < 4; i++) {
                try {
                    GradeQueryResult result = QueryGradeFromSystem(null, user.decryptUser(), i);
                    if (result != null) {
                        gradeCacheResult.getFirstTermGPAArray()[result.getYear()] = result.getFirstTermGPA();
                        gradeCacheResult.getSecondTermGPAArray()[result.getYear()] = result.getSecondTermGPA();
                        gradeCacheResult.getFirstTermIGPArray()[result.getYear()] = result.getFirstTermIGP();
                        gradeCacheResult.getSecondTermIGPArray()[result.getYear()] = result.getSecondTermIGP();
                        List<Grade> gradeList = new ArrayList<>();
                        gradeList.addAll(result.getFirstTermGradeList());
                        gradeList.addAll(result.getSecondTermGradeList());
                        gradeCacheResult.getGradeListArray()[result.getYear()] = gradeList;
                    }
                } catch (PasswordIncorrectException ignored) {

                } catch (Exception e) {
                    logger.error("定时查询保存成绩信息异常：", e);
                } finally {
                    countDownLatch.countDown();
                }
            }
            countDownLatch.await();
            return AsyncResult.forValue(gradeCacheResult);
        } catch (Exception e) {
            logger.error("定时查询保存成绩信息异常：", e);
        } finally {
            semaphore.release();
        }
        return AsyncResult.forValue(null);
    }
}
