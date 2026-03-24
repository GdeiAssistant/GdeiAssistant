package cn.gdeiassistant.core.gradequery.service;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.NotAvailableConditionException;
import cn.gdeiassistant.common.exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.common.pojo.Document.GradeDocument;
import cn.gdeiassistant.common.pojo.Entity.Grade;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.gradequery.pojo.GradeQueryResult;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.integration.edu.EduSystemClient;
import cn.gdeiassistant.integration.edu.pojo.EduSessionCredential;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GradeService {

    private final Logger logger = LoggerFactory.getLogger(GradeService.class);

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private EduSystemClient eduSystemClient;

    /**
     * 清空用户缓存的成绩信息
     *
     * @param sessionId
     */
    public void clearGrade(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        gradeDao.removeGrade(user.getUsername());
    }

    /**
     * 强制更新当前用户的成绩缓存（清空 MongoDB 缓存并实时从教务系统拉取一次）
     * 清空 MongoDB 缓存并实时从教务系统拉取一次。
     *
     * @param sessionId
     */
    public void updateGradeCache(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        String username = user.getUsername();
        // 清空缓存的成绩信息，下次查询时从教务系统实时获取
        gradeDao.removeGrade(username);
        // 主动触发一次实时查询，保证本次操作后前端能看到最新成绩（结果通过前端后续 fetchGrade 再拉取）
        // year 传 null，按最新学年处理
        queryGrade(sessionId, null);
    }

    /**
     * 查询成绩
     *
     * @param sessionId
     * @param year
     * @return
     * @throws Exception
     */
    public GradeQueryResult queryGrade(String sessionId, Integer year) throws Exception {
        //优先从缓存中获取
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        GradeDocument gradeDocument = gradeDao.queryGrade(user.getUsername());
        if (gradeDocument != null) {
            // 防御性空指针处理：任何一个 List 为空都降级为安全的空 List，避免 size()/get() NPE
            List<List<Grade>> gradeLists = gradeDocument.getGradeList();
            if (gradeLists == null) {
                gradeLists = new ArrayList<>();
                gradeDocument.setGradeList(gradeLists);
            }
            //若未指定查询学年，则默认查询最后学年的成绩信息
            if (year == null) {
                year = gradeLists.size() - 1;
            }
            if (gradeLists.size() == 0) {
                return null;
            }
            if (year >= gradeLists.size()) {
                // 学年越界时返回空状态，由前端自然渲染空页面，不抛错
                GradeQueryResult emptyResult = new GradeQueryResult();
                emptyResult.setYear(year);
                emptyResult.setFirstTermGradeList(new ArrayList<>());
                emptyResult.setSecondTermGradeList(new ArrayList<>());
                emptyResult.setFirstTermGPA(0.0);
                emptyResult.setSecondTermGPA(0.0);
                emptyResult.setFirstTermIGP(0.0);
                emptyResult.setSecondTermIGP(0.0);
                return emptyResult;
            }
            List<Grade> gradeList = gradeLists.get(year);
            List<Grade> firstTermGradeList = new ArrayList<>();
            List<Grade> secondTermGradeList = new ArrayList<>();
            for (Grade grade : gradeList) {
                if (grade.getGradeTerm().equals("1")) {
                    firstTermGradeList.add(grade);
                } else {
                    secondTermGradeList.add(grade);
                }
            }
            List<Double> firstTermGPAList = gradeDocument.getFirstTermGPAList();
            List<Double> secondTermGPAList = gradeDocument.getSecondTermGPAList();
            List<Double> firstTermIGPList = gradeDocument.getFirstTermIGPList();
            List<Double> secondTermIGPList = gradeDocument.getSecondTermIGPList();
            if (firstTermGPAList == null) {
                firstTermGPAList = new ArrayList<>();
                gradeDocument.setFirstTermGPAList(firstTermGPAList);
            }
            if (secondTermGPAList == null) {
                secondTermGPAList = new ArrayList<>();
                gradeDocument.setSecondTermGPAList(secondTermGPAList);
            }
            if (firstTermIGPList == null) {
                firstTermIGPList = new ArrayList<>();
                gradeDocument.setFirstTermIGPList(firstTermIGPList);
            }
            if (secondTermIGPList == null) {
                secondTermIGPList = new ArrayList<>();
                gradeDocument.setSecondTermIGPList(secondTermIGPList);
            }
            if (firstTermGPAList.size() <= year || secondTermGPAList.size() <= year
                    || firstTermIGPList.size() <= year || secondTermIGPList.size() <= year) {
                throw new NotAvailableConditionException("当前学年成绩缓存数据不完整，请重新查询");
            }
            Double firstTermGPA = firstTermGPAList.get(year);
            Double secondTermGPA = secondTermGPAList.get(year);
            Double firstTermIGP = firstTermIGPList.get(year);
            Double secondTermIGP = secondTermIGPList.get(year);
            GradeQueryResult GradeQueryResult = new GradeQueryResult();
            GradeQueryResult.setFirstTermGPA(firstTermGPA);
            GradeQueryResult.setFirstTermIGP(firstTermIGP);
            GradeQueryResult.setSecondTermGPA(secondTermGPA);
            GradeQueryResult.setSecondTermIGP(secondTermIGP);
            GradeQueryResult.setFirstTermGradeList(firstTermGradeList);
            GradeQueryResult.setSecondTermGradeList(secondTermGradeList);
            GradeQueryResult.setYear(year);
            return GradeQueryResult;
        }
        // 从教务系统实时获取（通过防腐层）
        UserCertificateEntity userCertificate = userCertificateService.getUserSessionCertificate(sessionId);
        EduSessionCredential credential = toEduCredential(userCertificate);
        GradeQueryResult GradeQueryResult = new GradeQueryResult();
        try {
            Document listPageDoc = eduSystemClient.fetchGradeListPage(sessionId, credential);
            List<String> yearList = new ArrayList<>();
            Element yearSelect = listPageDoc.getElementById("ddlXN");
            Elements yearOptions = yearSelect.select("option");
            for (Element element : yearOptions) {
                if (!element.text().trim().isEmpty()) {
                    yearList.add(element.text());
                }
            }
            Collections.reverse(yearList);
            if (yearList.isEmpty() || yearList.size() <= year || year < -1) {
                throw new NotAvailableConditionException("当前学年暂不可查询");
            }
            if (year == -1) {
                year = yearList.size() - 1;
            }
            GradeQueryResult.setYear(year);
            String viewState = listPageDoc.select("input[name=__VIEWSTATE]").val();
            String yearOptionValue = yearList.get(year);
            Document gradeDoc = eduSystemClient.fetchGradeByYear(sessionId, credential, viewState, yearOptionValue);
            Element element = gradeDoc.getElementsByClass("datelist").first();
            if (element == null) {
                throw new ServerErrorException("教务系统异常");
            }
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
                Element e = trs.get(i);
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
                    firstTermIGP = firstTermIGP + (Double.parseDouble(grade_credit) * Double.parseDouble(grade_gpa));
                    firstCreditSum = firstCreditSum + Double.parseDouble(grade_credit);
                    firstTermGradeList.add(grade);
                } else if (grade_term.equals("2")) {
                    secondTermIGP = secondTermIGP + (Double.parseDouble(grade_credit) * Double.parseDouble(grade_gpa));
                    secondCreditSum = secondCreditSum + Double.parseDouble(grade_credit);
                    secondTermGradeList.add(grade);
                }
            }
            if (firstCreditSum != 0) {
                firstTermGPA = firstTermIGP / firstCreditSum;
            }
            if (secondCreditSum != 0) {
                secondTermGPA = secondTermIGP / secondCreditSum;
            }
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            firstTermIGP = Double.parseDouble(decimalFormat.format(firstTermIGP));
            secondTermIGP = Double.parseDouble(decimalFormat.format(secondTermIGP));
            firstTermGPA = Double.parseDouble(decimalFormat.format(firstTermGPA));
            secondTermGPA = Double.parseDouble(decimalFormat.format(secondTermGPA));
            GradeQueryResult.setFirstTermIGP(firstTermIGP);
            GradeQueryResult.setFirstTermGPA(firstTermGPA);
            GradeQueryResult.setSecondTermIGP(secondTermIGP);
            GradeQueryResult.setSecondTermGPA(secondTermGPA);
            GradeQueryResult.setFirstTermGradeList(firstTermGradeList);
            GradeQueryResult.setSecondTermGradeList(secondTermGradeList);
            return GradeQueryResult;
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
        } catch (java.io.IOException e) {
            logger.error("课程成绩查询异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("课程成绩查询异常：", e);
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
