package cn.gdeiassistant.Service.GradeQuery;

import cn.gdeiassistant.Pojo.Document.GradeDocument;
import cn.gdeiassistant.Pojo.Entity.Grade;
import cn.gdeiassistant.Pojo.Entity.Privacy;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.GradeQuery.GradeCacheResult;
import cn.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy.PrivacyMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.User.UserMapper;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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
import java.util.concurrent.Semaphore;

@Service
@Profile("production")
public class GradeCronService {

    private Logger logger = LoggerFactory.getLogger(GradeCronService.class);

    private int currentUserStart = 0;

    private int gradeInterval;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivacyMapper privacyMapper;

    @Autowired
    private GradeDao gradeDao;

    @Value("#{propertiesReader['education.cache.grade.interval']}")
    public void setGradeInterval(int gradeInterval) {
        this.gradeInterval = gradeInterval;
    }

    /**
     * 同步教务系统实时成绩信息
     */
    @Scheduled(fixedDelay = 7200000)
    public void SynchronizeGradeData() {
        logger.info(LocalDateTime.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")) + "启动了查询保存用户成绩信息的任务");
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
                Privacy privacy = privacyMapper.selectPrivacy(user.getUsername());
                if (privacy != null && privacy.isCacheAllow()) {
                    GradeDocument gradeDocument = gradeDao.queryGradeByUsername(StringEncryptUtils
                            .decryptString(user.getUsername()));
                    //如果最后更新日期距今已超过7天，则进行更新
                    if (gradeDocument == null || Duration.between(gradeDocument.getUpdateDateTime()
                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), localDateTime).toDays() >= gradeInterval) {
                        ListenableFuture<GradeCacheResult> future = ((GradeService) AopContext.currentProxy())
                                .QueryGradeData(semaphore, user);
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
                                    logger.error("定时查询保存成绩信息异常：", e);
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            logger.error("定时查询保存成绩信息异常：", e);
        }
    }
}
