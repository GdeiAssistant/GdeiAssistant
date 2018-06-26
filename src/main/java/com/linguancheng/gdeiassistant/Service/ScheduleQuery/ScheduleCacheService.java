package com.linguancheng.gdeiassistant.Service.ScheduleQuery;

import com.linguancheng.gdeiassistant.Pojo.Document.ScheduleDocument;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryJsonResult;
import com.linguancheng.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

@Service
public class ScheduleCacheService {

    private int currentUserStart = 0;

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private UserMapper userMapper;

    private Log log = LogFactory.getLog(ScheduleCacheService.class);

    /**
     * 查询用户保存的课表信息
     *
     * @return
     */
    public ScheduleDocument ReadSchedule(String username) {
        return scheduleDao.queryScheduleByUsername(username);
    }

    /**
     * 定时查询并保存用户课表信息
     */
    @Scheduled(fixedDelay = 3600000)
    public void SaveSchedule() {
        try {
            Integer count = userMapper.selectUserCount();
            if (currentUserStart >= count) {
                currentUserStart = 0;
            }
            List<User> userList = userMapper.selectUserList(currentUserStart, 100);
            currentUserStart = currentUserStart + 100;
            LocalDateTime localDateTime = LocalDateTime.now();
            //设置线程信号量，限制最大同时查询的线程数为10
            Semaphore semaphore = new Semaphore(10);
            for (User user : userList) {
                ScheduleDocument scheduleDocument = scheduleDao.queryScheduleByUsername(StringEncryptUtils
                        .decryptString(user.getUsername()));
                //如果最后更新日期距今已超过3天，则进行更新
                if (scheduleDocument == null || Duration.between(scheduleDocument.getUpdateDateTime()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), localDateTime).toDays() >= 3) {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                    params.add("username", StringEncryptUtils.decryptString(user.getUsername()));
                    params.add("password", StringEncryptUtils.decryptString(user.getPassword()));
                    params.add("keycode", StringEncryptUtils.decryptString(user.getKeycode()));
                    params.add("number", StringEncryptUtils.decryptString(user.getNumber()));
                    params.add("refresh", String.valueOf(true));
                    params.add("week", String.valueOf(0));
                    semaphore.acquire();
                    ListenableFuture<ResponseEntity<ScheduleQueryJsonResult>> future = asyncRestTemplate
                            .postForEntity("https://www.gdeiassistant.cn/rest/schedulequery"
                                    , new HttpEntity<>(params, httpHeaders), ScheduleQueryJsonResult.class);
                    future.addCallback(new ListenableFutureCallback<ResponseEntity<ScheduleQueryJsonResult>>() {

                        @Override
                        public void onFailure(Throwable ex) {
                            log.error("定时查询保存课表信息异常：" + ex);
                            semaphore.release();
                        }

                        @Override
                        public void onSuccess(ResponseEntity<ScheduleQueryJsonResult> result) {
                            if (result.getBody().isSuccess()) {
                                ScheduleDocument document = new ScheduleDocument();
                                try {
                                    if (scheduleDocument != null && scheduleDocument.getId() != null) {
                                        document.setId(scheduleDocument.getId());
                                    }
                                    document.setUsername(StringEncryptUtils.decryptString(user.getUsername()));
                                    document.setScheduleList(result.getBody().getScheduleList());
                                    document.setUpdateDateTime(new Date());
                                    scheduleDao.saveSchedule(document);
                                } catch (Exception e) {
                                    log.error("定时查询保存课表信息异常：" + e);
                                } finally {
                                    semaphore.release();
                                }
                            }
                        }
                    });

                }
            }
        } catch (Exception e) {
            log.error("定时查询保存课表信息异常：" + e);
        }
    }
}
