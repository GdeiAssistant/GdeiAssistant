package cn.gdeiassistant.core.evaluate.service;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.EvaluateException.NotAvailableTimeException;
import cn.gdeiassistant.common.exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.integration.edu.EduSystemClient;
import cn.gdeiassistant.integration.edu.pojo.EduSessionCredential;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluateService {

    private final Logger logger = LoggerFactory.getLogger(EvaluateService.class);

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private EduSystemClient eduSystemClient;

    /**
     * 一键评教接口
     *
     * @param sessionId
     * @param directlySubmit
     */
    public void TeacherEvaluate(String sessionId, boolean directlySubmit)
            throws NetWorkTimeoutException, TimeStampIncorrectException
            , NotAvailableTimeException, ServerErrorException, PasswordIncorrectException {
        UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
        EduSessionCredential credential = toEduCredential(userCertificate);
        try {
            Document document = eduSystemClient.fetchEduMainPage(sessionId, credential);
            // 成功进入学生个人主页,获取所有评教列表
            Elements tops = document.getElementsByClass("nav").first().getElementsByClass("top");
            for (Element top : tops) {
                if (top.select("span").first().text().contains("教学评价")) {
                    Element sub = top.getElementsByClass("sub").first();
                    if (sub != null) {
                        Elements lis = sub.select("li");
                        List<String> evaluationList = new ArrayList<>();
                        if (lis != null && lis.size() > 0) {
                            for (Element li : lis) {
                                evaluationList.add(li.select("a").first().attr("href"));
                            }
                            document = eduSystemClient.fetchEduPage(sessionId, credential, evaluationList.get(0));
                            // 填写所有的评教信息并保存
                            for (String url : evaluationList) {
                                List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                                String __EVENTTARGET = document.getElementsByAttributeValue("name", "__EVENTTARGET").val();
                                String __EVENTARGUMENT = document.getElementsByAttributeValue("name", "__EVENTARGUMENT").val();
                                String __VIEWSTATE = document.getElementsByAttributeValue("name", "__VIEWSTATE").val();
                                basicNameValuePairList.add(new BasicNameValuePair("__EVENTTARGET", __EVENTTARGET));
                                basicNameValuePairList.add(new BasicNameValuePair("__EVENTARGUMENT", __EVENTARGUMENT));
                                basicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
                                String currentClass = ((url.split("&"))[0].split("="))[1];
                                basicNameValuePairList.add(new BasicNameValuePair("pjkc", currentClass));
                                Element table = document.getElementById("Table2");
                                Elements inputs = table.select("input");
                                Elements selects = table.select("select");
                                for (Element input : inputs) {
                                    basicNameValuePairList.add(new BasicNameValuePair(input.attr("name"), input.val()));
                                }
                                for (Element select : selects) {
                                    if (!select.attr("name").equals("pjkc")) {
                                        basicNameValuePairList.add(new BasicNameValuePair(select.attr("name"), "优秀"));
                                    }
                                }
                                basicNameValuePairList.add(new BasicNameValuePair("pjxx", ""));
                                basicNameValuePairList.add(new BasicNameValuePair("txt1", ""));
                                basicNameValuePairList.add(new BasicNameValuePair("TextBox1", "0"));
                                basicNameValuePairList.add(new BasicNameValuePair("Button1", "保  存"));
                                String formAction = document.getElementById("Form1").attr("action");
                                document = eduSystemClient.submitSpareRoomForm(sessionId, credential, formAction, basicNameValuePairList);
                            }
                            if (directlySubmit) {
                                List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                                String __EVENTTARGET = document.getElementsByAttributeValue("name", "__EVENTTARGET").val();
                                String __EVENTARGUMENT = document.getElementsByAttributeValue("name", "__EVENTARGUMENT").val();
                                String __VIEWSTATE = document.getElementsByAttributeValue("name", "__VIEWSTATE").val();
                                basicNameValuePairList.add(new BasicNameValuePair("__EVENTTARGET", __EVENTTARGET));
                                basicNameValuePairList.add(new BasicNameValuePair("__EVENTARGUMENT", __EVENTARGUMENT));
                                basicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
                                String currentClass = ((evaluationList.get(evaluationList.size() - 1).split("&"))[0].split("="))[1];
                                basicNameValuePairList.add(new BasicNameValuePair("pjkc", currentClass));
                                Element table = document.getElementById("Table2");
                                Elements inputs = table.select("input");
                                Elements selects = table.select("select");
                                for (Element input : inputs) {
                                    basicNameValuePairList.add(new BasicNameValuePair(input.attr("name"), input.val()));
                                }
                                for (Element select : selects) {
                                    if (!select.attr("name").equals("pjkc")) {
                                        basicNameValuePairList.add(new BasicNameValuePair(select.attr("name"), "优秀"));
                                    }
                                }
                                basicNameValuePairList.add(new BasicNameValuePair("pjxx", ""));
                                basicNameValuePairList.add(new BasicNameValuePair("txt1", ""));
                                basicNameValuePairList.add(new BasicNameValuePair("TextBox1", "0"));
                                basicNameValuePairList.add(new BasicNameValuePair("Button2", "提  交"));
                                String formAction = document.getElementById("Form1").attr("action");
                                eduSystemClient.submitSpareRoomForm(sessionId, credential, formAction, basicNameValuePairList);
                            }
                            return;
                        }
                        throw new NotAvailableTimeException("不是教学评价开放时间");
                    }
                    throw new NotAvailableTimeException("不是教学评价开放时间");
                }
            }
            throw new ServerErrorException("教务系统异常");
        } catch (IOException e) {
            logger.error("一键评教异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (TimeStampIncorrectException e) {
            logger.error("一键评教异常：", e);
            throw new TimeStampIncorrectException("时间戳校验失败");
        } catch (PasswordIncorrectException e) {
            logger.error("一键评教异常：", e);
            throw e;
        } catch (NotAvailableTimeException e) {
            logger.error("一键评教异常：", e);
            throw new NotAvailableTimeException("现在不是一键评教开放时间段");
        } catch (Exception e) {
            logger.error("一键评教异常：", e);
            throw new ServerErrorException("教务系统异常");
        }
    }

    private static EduSessionCredential toEduCredential(UserCertificateEntity entity) {
        EduSessionCredential cred = new EduSessionCredential();
        cred.setUsername(entity.getUser() != null ? entity.getUser().getUsername() : null);
        cred.setNumber(entity.getNumber());
        cred.setKeycode(entity.getKeycode());
        cred.setTimestamp(entity.getTimestamp());
        return cred;
    }
}
