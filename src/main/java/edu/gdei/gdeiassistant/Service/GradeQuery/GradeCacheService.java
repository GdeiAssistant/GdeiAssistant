package edu.gdei.gdeiassistant.Service.GradeQuery;

import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Document.GradeDocument;
import edu.gdei.gdeiassistant.Pojo.Entity.Grade;
import edu.gdei.gdeiassistant.Pojo.Entity.Privacy;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.GradeQuery.GradeCacheResult;
import edu.gdei.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import edu.gdei.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Privacy.PrivacyMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.User.UserMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Service
public class GradeCacheService {

    private Log log = LogFactory.getLog(GradeCacheService.class);

    private int currentUserStart = 0;

    private int gradeInterval;

    @Autowired
    private GradeQueryService gradeQueryService;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivacyMapper privacyMapper;

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
     * 清空用户保存的成绩信息
     *
     * @param username
     */
    public void ClearGrade(String username) {
        gradeDao.removeGrade(username);
    }

    @Async
    public ListenableFuture<GradeQueryResult> QueryGradeYearData(CountDownLatch countDownLatch
            , User user, int year) {
        try {
            return AsyncResult.forValue(gradeQueryService.QueryGradeFromSystem(null, user.decryptUser(), year));
        } catch (PasswordIncorrectException ignored) {

        } catch (Exception e) {
            log.error("定时查询保存成绩信息异常：", e);
        } finally {
            countDownLatch.countDown();
        }
        return AsyncResult.forValue(null);
    }

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
                ListenableFuture<GradeQueryResult> future = QueryGradeYearData(countDownLatch, user, i);
                future.addCallback(new ListenableFutureCallback<GradeQueryResult>() {

                    @Override
                    public void onFailure(Throwable ex) {

                    }

                    @Override
                    public void onSuccess(GradeQueryResult result) {
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
                    }
                });
            }
            countDownLatch.await();
            return AsyncResult.forValue(gradeCacheResult);
        } catch (Exception e) {
            log.error("定时查询保存成绩信息异常：", e);
        } finally {
            semaphore.release();
        }
        return AsyncResult.forValue(null);
    }

    /**
     * 定时查询并保存成绩信息
     */
    @Scheduled(fixedDelay = 7200000)
    public void SaveGrade() {
        log.info(LocalDateTime.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")) + "启动了查询保存用户成绩信息的任务");
        try {
            Integer count = userMapper.selectUserCount();
            if (currentUserStart >= count) {
                currentUserStart = 0;
            }
            List<User> userList = userMapper.selectUserList(currentUserStart, 50);
            currentUserStart = currentUserStart + 50;
            LocalDateTime localDateTime = LocalDateTime.now();
            //设置线程信号量，限制最大同时查询的线程数为5
            Semaphore semaphore = new Semaphore(5);
            for (User user : userList) {
                if (user.getGroup().equals(UserGroupEnum.STUDENT.getValue())
                        || user.getGroup().equals(UserGroupEnum.TEST.getValue())) {
                    Privacy privacy = privacyMapper.selectPrivacy(user.getUsername());
                    if (privacy != null && privacy.isCacheAllow()) {
                        GradeDocument gradeDocument = gradeDao.queryGradeByUsername(StringEncryptUtils
                                .decryptString(user.getUsername()));
                        //如果最后更新日期距今已超过7天，则进行更新
                        if (gradeDocument == null || Duration.between(gradeDocument.getUpdateDateTime()
                                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), localDateTime).toDays() >= gradeInterval) {
                            ListenableFuture<GradeCacheResult> future = QueryGradeData(semaphore, user);
                            future.addCallback(new ListenableFutureCallback<GradeCacheResult>() {

                                @Override
                                public void onFailure(Throwable ex) {

                                }

                                @Override
                                public void onSuccess(GradeCacheResult result) {
                                    try {
                                        if (result != null) {
                                            GradeDocument document = new GradeDocument();
                                            List<Double> firstTermGPAList = new ArrayList<>();
                                            List<Double> secondTermGPAList = new ArrayList<>();
                                            List<Double> firstTermIGPList = new ArrayList<>();
                                            List<Double> secondTermIGPList = new ArrayList<>();
                                            List<List<Grade>> gradeLists = new ArrayList<>();
                                            for (Double firstTermGPA : result.getFirstTermGPAArray()) {
                                                if (firstTermGPA != null) {
                                                    firstTermGPAList.add(firstTermGPA);
                                                }
                                            }
                                            for (Double secondTermGPA : result.getSecondTermGPAArray()) {
                                                if (secondTermGPA != null) {
                                                    secondTermGPAList.add(secondTermGPA);
                                                }
                                            }
                                            for (Double firstTermIGP : result.getFirstTermIGPArray()) {
                                                if (firstTermIGP != null) {
                                                    firstTermIGPList.add(firstTermIGP);
                                                }
                                            }
                                            for (Double secondTermIGP : result.getSecondTermIGPArray()) {
                                                if (secondTermIGP != null) {
                                                    secondTermIGPList.add(secondTermIGP);
                                                }
                                            }
                                            for (List<Grade> gradeList : result.getGradeListArray()) {
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
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("定时查询保存成绩信息异常：", e);
        }
    }
}
