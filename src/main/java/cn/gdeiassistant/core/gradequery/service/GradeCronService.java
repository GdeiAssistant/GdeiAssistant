package cn.gdeiassistant.core.gradequery.service;

import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.pojo.Document.GradeDocument;
import cn.gdeiassistant.common.pojo.Entity.Grade;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.gradequery.pojo.GradeCacheResult;
import cn.gdeiassistant.core.gradequery.pojo.GradeQueryResult;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.cron.mapper.CronMapper;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Service
@Profile("production")
public class GradeCronService {

    private final Logger logger = LoggerFactory.getLogger(GradeCronService.class);

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private CronMapper cronMapper;

    @Autowired
    private GradeDao gradeDao;

    /**
     * 同步教务系统实时成绩信息（可由 Scheduler 或 HTTP /cron/grade 触发）。
     */
    public void synchronizeGradeData() {
        logger.info(LocalDateTime.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")) + "启动了查询保存用户成绩信息的任务");
        try {
            //获取所有允许教务缓存的用户
            List<User> userList = cronMapper.selectCacheAllowUsers();
            //设置线程信号量，限制最大同时查询的线程数为5
            Semaphore semaphore = new Semaphore(5);
            for (User user : userList) {
                GradeDocument gradeDocument = gradeDao.queryGrade(user.getUsername());
                //如果最后更新日期距今已超过7天，则进行更新
                if (gradeDocument == null || Duration.between(gradeDocument.getUpdateDateTime()
                                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        , LocalDateTime.now()).toDays() >= 7) {
                    CompletableFuture<GradeCacheResult> future = ((GradeCronService) AopContext.currentProxy())
                            .asyncQueryGrade(semaphore, user);
                    User finalUser = user;
                    future.whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("定时查询保存成绩信息异常：", throwable);
                            return;
                        }
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
                                document.setUsername(finalUser.getUsername());
                                document.setFirstTermGPAList(firstTermGPAList);
                                document.setFirstTermIGPList(firstTermIGPList);
                                document.setSecondTermGPAList(secondTermGPAList);
                                document.setSecondTermIGPList(secondTermIGPList);
                                document.setGradeList(gradeLists);
                                document.setUpdateDateTime(new Date());
                                gradeDao.saveGrade(document);
                            }
                        } catch (Exception e) {
                            logger.error("定时查询保存成绩信息异常：", e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            logger.error("定时查询保存成绩信息异常：", e);
        }
    }

    /**
     * 异步获取教务系统成绩信息
     *
     * @param semaphore
     * @param user
     * @return
     */
    @Async
    public CompletableFuture<GradeCacheResult> asyncQueryGrade(Semaphore semaphore, User user) {
        boolean acquired = false;
        try {
            semaphore.acquire();
            acquired = true;
            GradeCacheResult gradeCacheResult = new GradeCacheResult();
            gradeCacheResult.setFirstTermGPAArray(new Double[4]);
            gradeCacheResult.setSecondTermGPAArray(new Double[4]);
            gradeCacheResult.setFirstTermIGPArray(new Double[4]);
            gradeCacheResult.setSecondTermIGPArray(new Double[4]);
            gradeCacheResult.setGradeListArray(new ArrayList[4]);
            CountDownLatch countDownLatch = new CountDownLatch(4);
            for (int i = 0; i < 4; i++) {
                try {
                    //生成UUID作为临时SessionId
                    String sessionId = UUID.randomUUID().toString().replace("-", "");
                    //用户登录
                    userLoginService.userLogin(sessionId, user.getUsername(), user.getPassword());
                    //查询成绩信息
                    GradeQueryResult result = gradeService.queryGrade(sessionId, i);
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
            return CompletableFuture.completedFuture(gradeCacheResult);
        } catch (Exception e) {
            logger.error("定时查询保存成绩信息异常：", e);
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
