package cn.gdeiassistant.Service.ScheduleQuery;

import cn.gdeiassistant.Pojo.Document.ScheduleDocument;
import cn.gdeiassistant.Pojo.Entity.Privacy;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import cn.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

@Service
@Profile("production")
public class ScheduleCronService {

    private Logger logger = LoggerFactory.getLogger(ScheduleCronService.class);

    private int currentUserStart = 0;

    private int scheduleInterval;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivacyMapper privacyMapper;

    @Value("#{propertiesReader['education.cache.schedule.interval']}")
    public void setScheduleInterval(int scheduleInterval) {
        this.scheduleInterval = scheduleInterval;
    }

    @Autowired
    private ScheduleDao scheduleDao;

    /**
     * 同步教务系统实时课表信息
     */
    @Scheduled(fixedDelay = 7200000)
    public void SynchronizeScheduleData() {
        logger.info("{}启动了查询保存用户课表信息的任务", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
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
                        ListenableFuture<ScheduleQueryResult> future = ((ScheduleService) AopContext.currentProxy())
                                .QueryScheduleData(semaphore, user);
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
                                    logger.error("定时查询保存课表信息异常：", e);
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            logger.error("定时查询保存课表信息异常：", e);
        }
    }
}
