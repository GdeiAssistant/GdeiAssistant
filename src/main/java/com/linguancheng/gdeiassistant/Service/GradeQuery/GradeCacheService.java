package com.linguancheng.gdeiassistant.Service.GradeQuery;

import com.linguancheng.gdeiassistant.Pojo.Document.GradeDocument;
import com.linguancheng.gdeiassistant.Pojo.Entity.Grade;
import com.linguancheng.gdeiassistant.Pojo.Entity.Privacy;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQueryJsonResult;
import com.linguancheng.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy.PrivacyMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Service
public class GradeCacheService {

    private int currentUserStart = 0;

    private int gradeInterval;

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivacyMapper privacyMapper;

    private Log log = LogFactory.getLog(GradeCacheService.class);

    @Value("#{propertiesReader['education.cache.grade.interval']}")
    public void setGradeInterval(int gradeInterval) {
        this.gradeInterval = gradeInterval;
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
     * 定时查询并保存成绩信息
     */
    @Scheduled(fixedDelay = 3600000)
    public void SaveGrade() {
        try {
            Integer count = userMapper.selectUserCount();
            if (currentUserStart >= count) {
                currentUserStart = 0;
            }
            List<User> userList = userMapper.selectUserList(currentUserStart, 100);
            currentUserStart = currentUserStart + 100;
            LocalDateTime localDateTime = LocalDateTime.now();
            //设置线程信号量，限制最大同时查询的线程数为10
            for (User user : userList) {
                Privacy privacy = privacyMapper.selectPrivacy(user.getUsername());
                if (privacy.isCache()) {
                    GradeDocument gradeDocument = gradeDao.queryGradeByUsername(StringEncryptUtils
                            .decryptString(user.getUsername()));
                    //如果最后更新日期距今已超过7天，则进行更新
                    if (gradeDocument == null || Duration.between(gradeDocument.getUpdateDateTime()
                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), localDateTime).toDays() >= gradeInterval) {
                        try {
                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                            GradeDocument.GradeList gradeArrays[] = new GradeDocument.GradeList[4];
                            Double firstTermGPA_Arrays[] = new Double[4];
                            Double firstTermIGP_Arrays[] = new Double[4];
                            Double secondTermGPA_Arrays[] = new Double[4];
                            Double secondTermIGP_Arrays[] = new Double[4];
                            CountDownLatch countDownLatch = new CountDownLatch(4);
                            Boolean[] hasError = {false};
                            for (int i = 0; i < 4; i++) {
                                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                                params.add("username", StringEncryptUtils.decryptString(user.getUsername()));
                                params.add("password", StringEncryptUtils.decryptString(user.getPassword()));
                                params.add("method", String.valueOf(1));
                                params.add("year", String.valueOf(i));
                                ListenableFuture<ResponseEntity<GradeQueryJsonResult>> future = asyncRestTemplate
                                        .postForEntity("https://www.gdeiassistant.cn/rest/gradequery"
                                                , new HttpEntity<>(params, httpHeaders), GradeQueryJsonResult.class);
                                future.addCallback(new ListenableFutureCallback<ResponseEntity<GradeQueryJsonResult>>() {

                                    @Override
                                    public void onFailure(Throwable ex) {
                                        log.error("定时查询保存成绩信息异常：", ex);
                                        countDownLatch.countDown();
                                    }

                                    @Override
                                    public void onSuccess(ResponseEntity<GradeQueryJsonResult> result) {
                                        if (result.getBody().isSuccess()) {
                                            List<Grade> grades = new ArrayList<>();
                                            Integer queryYear = result.getBody().getQueryYear();
                                            if (!result.getBody().getFirstTermGradeList().isEmpty()) {
                                                grades.addAll(result.getBody().getFirstTermGradeList());
                                            }
                                            if (!result.getBody().getSecondTermGradeList().isEmpty()) {
                                                grades.addAll(result.getBody().getSecondTermGradeList());
                                            }
                                            if (!grades.isEmpty()) {
                                                gradeArrays[queryYear] = new GradeDocument.GradeList(grades);
                                            }
                                            firstTermGPA_Arrays[queryYear] = result.getBody().getFirstTermGPA();
                                            firstTermIGP_Arrays[queryYear] = result.getBody().getFirstTermIGP();
                                            secondTermGPA_Arrays[queryYear] = result.getBody().getSecondTermGPA();
                                            secondTermIGP_Arrays[queryYear] = result.getBody().getSecondTermIGP();
                                        } else {
                                            if (!result.getBody().isEmpty()) {
                                                hasError[0] = true;
                                            }
                                        }
                                        countDownLatch.countDown();
                                    }
                                });
                            }
                            countDownLatch.await();
                            if (!hasError[0]) {
                                GradeDocument document = new GradeDocument();
                                List<Double> firstTermGPAList = new ArrayList<>();
                                List<Double> secondTermGPAList = new ArrayList<>();
                                List<Double> firstTermIGPList = new ArrayList<>();
                                List<Double> secondTermIGPList = new ArrayList<>();
                                List<GradeDocument.GradeList> gradeLists = new ArrayList<>();
                                for (Double firstTermGPA : firstTermGPA_Arrays) {
                                    if (firstTermGPA != null) {
                                        firstTermGPAList.add(firstTermGPA);
                                    }
                                }
                                for (Double secondTermGPA : secondTermGPA_Arrays) {
                                    if (secondTermGPA != null) {
                                        secondTermGPAList.add(secondTermGPA);
                                    }
                                }
                                for (Double firstTermIGP : firstTermIGP_Arrays) {
                                    if (firstTermIGP != null) {
                                        firstTermIGPList.add(firstTermIGP);
                                    }
                                }
                                for (Double secondTermIGP : secondTermIGP_Arrays) {
                                    if (secondTermIGP != null) {
                                        secondTermIGPList.add(secondTermIGP);
                                    }
                                }
                                for (GradeDocument.GradeList gradeList : gradeArrays) {
                                    if (gradeList != null) {
                                        gradeLists.add(gradeList);
                                    }
                                }
                                if (gradeDocument != null && gradeDocument.getId() != null) {
                                    document.setId(gradeDocument.getId());
                                }
                                document.setUsername(StringEncryptUtils.decryptString(user.getUsername()));
                                document.setFirstTermGPAList(firstTermGPAList);
                                document.setFirstTermIGPList(firstTermIGPList);
                                document.setSecondTermGPAList(secondTermGPAList);
                                document.setSecondTermIGPList(secondTermIGPList);
                                document.setGradeList(gradeLists);
                                document.setUpdateDateTime(new Date());
                                gradeDao.saveGrade(document);
                            }
                        } catch (Exception e) {
                            log.error("定时查询保存成绩信息异常：", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("定时查询保存成绩信息异常：", e);
        }
    }
}
