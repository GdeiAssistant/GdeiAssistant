package cn.gdeiassistant.core.spareRoom.service;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.common.exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.core.spareRoom.pojo.dto.EmptyClassroomQueryDTO;
import cn.gdeiassistant.core.spareRoom.pojo.vo.SpareRoomVO;
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
public class SpareRoomService {

    private final Logger logger = LoggerFactory.getLogger(SpareRoomService.class);

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private EduSystemClient eduSystemClient;

    /**
     * 查询空课室信息
     */
    public List<SpareRoomVO> querySpareRoom(String sessionId, EmptyClassroomQueryDTO query)
            throws NetWorkTimeoutException, TimeStampIncorrectException
            , ServerErrorException, ErrorQueryConditionException, PasswordIncorrectException {
        UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
        EduSessionCredential credential = toEduCredential(userCertificate);
        try {
            Document document = eduSystemClient.fetchSpareRoomInitialDocument(sessionId, credential);
            int pageIndex = 1;
            int maxIndex = 1;
            List<SpareRoomVO> spareRoomList = new ArrayList<>();
            while (pageIndex <= maxIndex) {
                Element Form1 = document.getElementsByAttributeValue("name", "Form1").first();
                String formAction = Form1.attr("action");
                String __EVENTTARGET = Form1.getElementsByAttributeValue("name", "__EVENTTARGET").first().val();
                String __EVENTARGUMENT = Form1.getElementsByAttributeValue("name", "__EVENTARGUMENT").first().val();
                String __VIEWSTATE = Form1.getElementsByAttributeValue("name", "__VIEWSTATE").first().val();
                String xiaoq = Form1.getElementById("xiaoq").select("option").get(query.getZone()).attr("value");
                String jslb = Form1.getElementById("jslb").select("option").get(query.getType()).attr("value");
                String min_zws = query.getMinSeating() == null ? "0" : String.valueOf(query.getMinSeating());
                String max_zws = query.getMaxSeating() == null ? "" : String.valueOf(query.getMaxSeating());
                String ddlKsz = String.valueOf(query.getStartTime());
                String ddlJsz = String.valueOf(query.getEndTime());
                Elements xqjOptions = Form1.getElementById("xqj").select("option");
                String xqj = null;
                for (Element xqjOption : xqjOptions) {
                    if (Integer.valueOf(xqjOption.attr("value")) == query.getMinWeek() + 1) {
                        xqj = xqjOption.attr("value");
                    }
                }
                if (xqj == null) {
                    throw new ErrorQueryConditionException("错误的查询条件");
                }
                String ddlXqm = null;
                for (Element xqjOption : xqjOptions) {
                    if (Integer.valueOf(xqjOption.attr("value")) == query.getMaxWeek() + 1) {
                        ddlXqm = xqjOption.attr("value");
                    }
                }
                if (ddlXqm == null) {
                    throw new ErrorQueryConditionException("错误的查询条件");
                }
                String weekTypeString = null;
                switch (query.getWeekType()) {
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
                String sjd = Form1.getElementById("sjd").select("option").get(query.getClassNumber()).attr("value");
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

                document = eduSystemClient.submitSpareRoomForm(sessionId, credential, formAction, basicNameValuePairList);

                Elements trs = document.select("table").first().select("tr");
                if (trs.size() <= 1) {
                    return null;
                }
                if (pageIndex == 1) {
                    maxIndex = Integer.valueOf(document.getElementById("dpDataGrid1_lblTotalPages").text());
                }
                for (int j = 1; j < trs.size(); j++) {
                    Elements tds = trs.get(j).select("td");
                    SpareRoomVO vo = new SpareRoomVO();
                    vo.setNumber(tds.first().text());
                    vo.setName(tds.get(1).text());
                    vo.setType(tds.get(2).text());
                    switch (Integer.valueOf(tds.get(3).text())) {
                        case 1:
                            vo.setZone("海珠");
                            break;
                        case 4:
                            vo.setZone("花都");
                            break;
                        case 8:
                            vo.setZone("广东轻工南海校区");
                            break;
                        case 9:
                            vo.setZone("业余函授校区");
                            break;
                        default:
                            vo.setZone("其他");
                            break;
                    }
                    vo.setClassSeating(tds.get(4).text());
                    vo.setSection(tds.get(5).text());
                    vo.setExamSeating(tds.get(6).text());
                    spareRoomList.add(vo);
                }
                pageIndex++;
            }
            return spareRoomList;
        } catch (IOException e) {
            logger.error("查询空课室异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (TimeStampIncorrectException e) {
            logger.error("查询空课室异常：", e);
            throw new TimeStampIncorrectException("时间戳校验失败");
        } catch (PasswordIncorrectException e) {
            logger.error("查询空课室异常：", e);
            throw e;
        } catch (ErrorQueryConditionException e) {
            logger.error("查询空课室异常：", e);
            throw new ErrorQueryConditionException("查询条件错误");
        } catch (Exception e) {
            logger.error("查询空课室异常：", e);
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
