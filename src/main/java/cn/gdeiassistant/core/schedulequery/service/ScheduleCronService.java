package cn.gdeiassistant.core.schedulequery.service;

import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.pojo.Document.ScheduleDocument;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.schedulequery.pojo.ScheduleQueryResult;
import cn.gdeiassistant.core.schedulequery.pojo.vo.ScheduleQueryVO;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

@Service
@Profile("production")
public class ScheduleCronService {

    private final Logger logger = LoggerFactory.getLogger(ScheduleCronService.class);

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CronMapper cronMapper;

    @Autowired
    private ScheduleDao scheduleDao;

    /**
     * 同步教务系统实时课表信息（可由 Scheduler 或 HTTP /cron/schedule 触发）。
     */
    public void synchronizeScheduleData() {
        logger.info("{}启动了查询保存用户课表信息的任务", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        try {
            List<User> userList = cronMapper.selectCacheAllowUsers();
            //设置线程信号量，限制最大同时查询的线程数为5
            Semaphore semaphore = new Semaphore(5);
            for (User user : userList) {
                ScheduleDocument scheduleDocument = scheduleDao.querySchedule(user.getUsername());
                //如果最后更新日期距今已超过3天，则进行更新
                if (scheduleDocument == null || Duration.between(scheduleDocument.getUpdateDateTime()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), LocalDateTime.now()).toDays() >= 3) {
                    CompletableFuture<ScheduleQueryVO> future = ((ScheduleCronService) AopContext.currentProxy())
                            .asyncQuerySchedule(semaphore, user);
                    User finalUser = user;
                    future.whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logger.error("定时查询保存课表信息异常：", throwable);
                            return;
                        }
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
    public CompletableFuture<ScheduleQueryVO> asyncQuerySchedule(Semaphore semaphore, User user) {
        boolean acquired = false;
        try {
            semaphore.acquire();
            acquired = true;
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            userLoginService.userLogin(sessionId, user.getUsername(), user.getPassword());
            ScheduleQueryResult result = scheduleService.querySchedule(sessionId, 0);
            ScheduleQueryVO vo = result != null ? new ScheduleQueryVO(result.getScheduleList(), result.getWeek() != null ? result.getWeek() : 0) : null;
            return CompletableFuture.completedFuture(vo);
        } catch (PasswordIncorrectException ignored) {

        } catch (Exception e) {
            logger.error("定时查询保存课表信息异常：", e);
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
