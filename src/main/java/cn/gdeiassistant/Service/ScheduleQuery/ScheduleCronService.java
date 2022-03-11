package cn.gdeiassistant.Service.ScheduleQuery;

import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Pojo.Document.ScheduleDocument;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import cn.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Cron.CronMapper;
import cn.gdeiassistant.Service.UserLogin.UserLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Service
@Profile("production")
public class ScheduleCronService {

    private Logger logger = LoggerFactory.getLogger(ScheduleCronService.class);

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CronMapper cronMapper;

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
            List<User> userList = cronMapper.selectCacheAllowUsers();
            //设置线程信号量，限制最大同时查询的线程数为5
            Semaphore semaphore = new Semaphore(5);
            for (User user : userList) {
                user = user.decryptUser();
                ScheduleDocument scheduleDocument = scheduleDao.querySchedule(user.getUsername());
                //如果最后更新日期距今已超过3天，则进行更新
                if (scheduleDocument == null || Duration.between(scheduleDocument.getUpdateDateTime()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), LocalDateTime.now()).toDays() >= 3) {
                    ListenableFuture<ScheduleQueryResult> future = ((ScheduleCronService) AopContext.currentProxy())
                            .AsyncQuerySchedule(semaphore, user);
                    User finalUser = user;
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
                                    document.setUsername(finalUser.getUsername());
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
        } catch (Exception e) {
            logger.error("定时查询保存课表信息异常：", e);
        }
    }

    /**
     * 异步获取教务系统课表信息
     *
     * @param semaphore
     * @param user
     */
    @Async
    public ListenableFuture<ScheduleQueryResult> AsyncQuerySchedule(Semaphore semaphore, User user) {
        try {
            semaphore.acquire();
            //生成UUID作为临时SessionId
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            //用户登录
            userLoginService.UserLogin(sessionId, user.getUsername(), user.getPassword());
            //查询课表
            ScheduleQueryResult scheduleQueryResult = scheduleService
                    .QuerySchedule(sessionId, 0);
            return AsyncResult.forValue(scheduleQueryResult);
        } catch (PasswordIncorrectException ignored) {

        } catch (Exception e) {
            logger.error("定时查询保存课表信息异常：", e);
        } finally {
            semaphore.release();
        }
        return AsyncResult.forValue(null);
    }
}
