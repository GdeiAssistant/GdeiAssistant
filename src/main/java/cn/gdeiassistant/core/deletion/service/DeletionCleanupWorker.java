package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.common.pojo.entity.CloseLog;
import cn.gdeiassistant.core.close.mapper.CloseMapper;
import cn.gdeiassistant.core.deletion.mapper.DeletionCleanupMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Post-commit cross-store cleanup: credentials, live connections, external caches and the
 * close log. Domain work is delegated to {@link AccountLifecycleContributor}; this class
 * only claims tasks, retries them and owns the transaction boundary.
 */
@Service
public class DeletionCleanupWorker {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DeletionCleanupWorker.class);

    public record CleanupRequested(String id) {
    }

    private final DeletionCleanupMapper mapper;
    private final UserMapper userMapper;
    private final PlatformTransactionManager transactions;
    private final UserCertificateService certificateService;
    private final CloseMapper closeMapper;
    private final List<AccountLifecycleContributor> contributors;

    public DeletionCleanupWorker(DeletionCleanupMapper mapper,
                                 UserMapper userMapper,
                                 @Qualifier("appTransactionManager") PlatformTransactionManager transactions,
                                 UserCertificateService certificateService,
                                 CloseMapper closeMapper,
                                 List<AccountLifecycleContributor> contributors) {
        this.mapper = mapper;
        this.userMapper = userMapper;
        this.transactions = transactions;
        this.certificateService = certificateService;
        this.closeMapper = closeMapper;
        this.contributors = contributors;
    }

    @TransactionalEventListener
    public void afterCommit(CleanupRequested request) {
        runOne(request.id());
    }

    @Scheduled(fixedDelayString = "${account-deletion.cleanup-delay-ms:60000}")
    public void retryPending() {
        try {
            for (String id : mapper.pendingIds()) {
                runOne(id);
            }
        } catch (Exception failure) {
            LOGGER.warn("Account cleanup queue unavailable: {}", failure.getClass().getSimpleName());
        }
    }

    public void runOne(String id) {
        try {
            TransactionTemplate transaction = new TransactionTemplate(transactions);
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
            if (mapper.claim(id) != 1) {
                return;
            }
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
            AccountClosure closure = new AccountClosure(
                    task.getUsername(), task.getResetname(), task.getUserId());
            for (AccountLifecycleContributor contributor : contributors) {
                contributor.cleanupAfterCommit(closure);
            }
            CloseLog log = new CloseLog();
            log.setUsername(task.getUsername());
            log.setResetname(task.getResetname());
            closeMapper.insertCloseLog(log);
            mapper.complete(id);
        } catch (Exception failure) {
            if (claimed) {
                try {
                    mapper.retry(id, failure.getClass().getSimpleName());
                } catch (Exception unavailable) {
                    LOGGER.warn("Account cleanup retry deferred: {}", unavailable.getClass().getSimpleName());
                }
            } else {
                LOGGER.warn("Account cleanup claim deferred: {}", failure.getClass().getSimpleName());
            }
        }
    }
}
