package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.core.deletion.mapper.DeletionCleanupMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.close.mapper.CloseMapper;
import cn.gdeiassistant.core.social.websocket.SocialRealtimeHub;
import cn.gdeiassistant.common.pojo.entity.CloseLog;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;

/** Cross-store cleanup runs after the app transaction and survives process restarts. */
@Service
public class DeletionCleanupWorker {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DeletionCleanupWorker.class);
    public record CleanupRequested(String id) {}
    @Autowired private DeletionCleanupMapper mapper;
    @Autowired private UserMapper userMapper;
    @Autowired
    @Qualifier("appTransactionManager")
    private PlatformTransactionManager transactions;
    @Autowired private GradeDao gradeDao;
    @Autowired private ScheduleDao scheduleDao;
    @Autowired private UserProfileService profileService;
    @Autowired private UserCertificateService certificateService;
    @Autowired private CloseMapper closeMapper;
    @Autowired(required=false) private SocialRealtimeHub realtimeHub;

    @TransactionalEventListener
    public void afterCommit(CleanupRequested request) { runOne(request.id()); }

    @Scheduled(fixedDelayString="${account-deletion.cleanup-delay-ms:60000}")
    public void retryPending() {
        try {
            for (String id : mapper.pendingIds()) runOne(id);
        } catch (Exception failure) {
            LOGGER.warn("Account cleanup queue unavailable: {}", failure.getClass().getSimpleName());
        }
    }

    public void runOne(String id) {
        try {
            var transaction = new TransactionTemplate(transactions);
            transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            // The claim row stays locked through every delete and the final queue update.
            // REQUIRES_NEW also commits writes made from the afterCommit callback.
            transaction.executeWithoutResult(status -> cleanClaimedTask(id));
        } catch (Exception failure) {
            LOGGER.warn("Account cleanup transaction deferred: {}", failure.getClass().getSimpleName());
        }
    }

    private void cleanClaimedTask(String id) {
        boolean claimed = false;
        try {
            if (mapper.claim(id) != 1) return;
            claimed = true;
            var task = mapper.find(id);
            if (!userMapper.selectExistingUsernames(List.of(task.getUsername())).isEmpty()) {
                // Existing pre-fix username reuse needs identity-specific reconciliation.
                mapper.retry(id, "ACTIVE_USERNAME_CONFLICT");
                LOGGER.warn("Account cleanup deferred: ACTIVE_USERNAME_CONFLICT");
                return;
            }
            // Repeating these operations is safe; no credential or external error text is stored.
            certificateService.clearReusableCredentials(task.getUsername());
            if (task.getUserId() != null && realtimeHub != null) realtimeHub.disconnectUser(task.getUserId());
            gradeDao.removeGrade(task.getUsername());
            scheduleDao.removeSchedule(task.getUsername());
            profileService.deleteAvatarForUsername(task.getUsername());
            CloseLog log = new CloseLog();
            log.setUsername(task.getUsername());
            log.setResetname(task.getResetname());
            closeMapper.insertCloseLog(log);
            mapper.complete(id);
        } catch (Exception failure) {
            if (claimed) {
                try { mapper.retry(id, failure.getClass().getSimpleName()); }
                catch (Exception unavailable) { LOGGER.warn("Account cleanup retry deferred: {}", unavailable.getClass().getSimpleName()); }
            } else LOGGER.warn("Account cleanup claim deferred: {}", failure.getClass().getSimpleName());
        }
    }
}
