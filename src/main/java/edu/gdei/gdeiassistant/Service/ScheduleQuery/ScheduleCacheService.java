package edu.gdei.gdeiassistant.Service.ScheduleQuery;

import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Document.ScheduleDocument;
import edu.gdei.gdeiassistant.Pojo.Entity.Privacy;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import edu.gdei.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy.PrivacyMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

@Service
public class ScheduleCacheService {

    private Log log = LogFactory.getLog(ScheduleCacheService.class);

    private int currentUserStart = 0;

    private int scheduleInterval;

    @Autowired
    private ScheduleQueryService scheduleQueryService;

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivacyMapper privacyMapper;

    @Value("#{propertiesReader['education.cache.schedule.interval']}")
    public void setScheduleInterval(int scheduleInterval) {
        this.scheduleInterval = scheduleInterval;
    }

    /**
     * 清空缓存的课表信息
     *
     * @param username
     */
    public void ClearSchedule(String username) {
        scheduleDao.removeSchedule(username);
    }

    /**
     * 异步获取教务系统课表信息任务
     *
     * @param semaphore
     * @param user
     */
    @Async
    public ListenableFuture<ScheduleQueryResult> QueryScheduleData(Semaphore semaphore, User user) {
        try {
            semaphore.acquire();
            return AsyncResult.forValue(scheduleQueryService
                    .QueryScheduleFromSystem(null, user.decryptUser(), 0));
        } catch (PasswordIncorrectException ignored) {

        } catch (Exception e) {
            log.error("定时查询保存课表信息异常：", e);
        } finally {
            semaphore.release();
        }
        return AsyncResult.forValue(null);
    }

    /**
     * 定时查询并保存用户课表信息
     */
    @Scheduled(fixedDelay = 7200000)
    public void SaveSchedule() {
        log.info(LocalDateTime.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")) + "启动了查询保存用户课表信息的任务");
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
                    ScheduleDocument scheduleDocument = scheduleDao.querySchedule(StringEncryptUtils
                            .decryptString(user.getUsername()));
                    //如果最后更新日期距今已超过3天，则进行更新
                    if (scheduleDocument == null || Duration.between(scheduleDocument.getUpdateDateTime()
                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), localDateTime).toDays() >= scheduleInterval) {
                        ListenableFuture<ScheduleQueryResult> future = QueryScheduleData(semaphore, user);
                        future.addCallback(new ListenableFutureCallback<ScheduleQueryResult>() {

                            @Override
                            public void onFailure(Throwable ex) {

                            }

                            @Override
                            public void onSuccess(ScheduleQueryResult result) {
                                try {
                                    if (result != null) {
                                        ScheduleDocument document = new ScheduleDocument();
                                        if (scheduleDocument != null && scheduleDocument.getId() != null) {
                                            document.setId(scheduleDocument.getId());
                                        }
                                        document.setUsername(StringEncryptUtils.decryptString(user.getUsername()));
                                        document.setScheduleList(result.getScheduleList());
                                        document.setUpdateDateTime(new Date());
                                        scheduleDao.saveSchedule(document);
                                    }
                                } catch (Exception e) {
                                    log.error("定时查询保存课表信息异常：", e);
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("定时查询保存课表信息异常：", e);
        }
    }
}
