package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountCloseBlocker;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.databaseexception.UserNotExistException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import cn.gdeiassistant.core.deletion.mapper.DeletionCleanupMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import cn.gdeiassistant.common.i18n.Messages;

/**
 * Orchestrates account closure: validate, lock, let every domain erase its own data,
 * close the account and enqueue cross-store cleanup. Which rows count as this user's
 * data is decided by each domain through {@link AccountLifecycleContributor}.
 */
@Service
public class AccountDeletionService {

    private static final String ANONYMIZED_PREFIX = "del_";

    private final List<AccountLifecycleContributor> contributors;
    private final UserCertificateService userCertificateService;
    private final UserMapper userMapper;
    private final DeletionCleanupMapper cleanupMapper;
    private final ApplicationEventPublisher events;

    public AccountDeletionService(List<AccountLifecycleContributor> contributors,
                                  UserCertificateService userCertificateService,
                                  UserMapper userMapper,
                                  DeletionCleanupMapper cleanupMapper,
                                  ApplicationEventPublisher events) {
        this.contributors = contributors;
        this.userCertificateService = userCertificateService;
        this.userMapper = userMapper;
        this.cleanupMapper = cleanupMapper;
        this.events = events;
    }

    /**
     * Check whether the account may be closed; returns each domain's reasons and advice.
     */
    public DataJsonResult<Map<String, String>> checkAccountDeletability(String sessionId, String password)
            throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        CampusAccountView queryUser = userMapper.selectUser(user.getUsername());
        if (queryUser == null) {
            throw new UserNotExistException(Messages.get("deletion.userMissing"));
        }
        if (password == null || password.isBlank()) {
            throw new PasswordIncorrectException(Messages.get("deletion.campusPasswordRequired"));
        }
        userCertificateService.verifyCurrentPassword(user.getUsername(), password);
        Map<String, String> blockers = new LinkedHashMap<>();
        for (AccountLifecycleContributor contributor : contributors) {
            for (AccountCloseBlocker blocker : contributor.blockers(user.getUsername())) {
                blockers.put(blocker.reason(), blocker.advice());
            }
        }
        if (blockers.isEmpty()) {
            return new DataJsonResult<>(true);
        }
        return new DataJsonResult<>(false, blockers);
    }

    /**
     * Close the user account.
     */
    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    public void deleteAccount(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        CampusAccountView existing = userMapper.selectUser(user.getUsername());
        // Same ordered app_user row lock that sending and following take: once closed,
        // the sending side re-reads under the lock and gets CONTACT_UNAVAILABLE.
        if (existing != null && existing.getId() != null) {
            userMapper.selectUserByIdForUpdate(existing.getId());
        }
        AccountClosure closure = new AccountClosure(
                user.getUsername(),
                nextAnonymizedName(user.getUsername()),
                existing == null ? null : existing.getId());
        for (AccountLifecycleContributor contributor : contributors) {
            contributor.purge(closure);
        }
        if (closure.userId() != null) {
            userMapper.closeAppUser(closure.userId());
        }
        userMapper.closeUser(closure.anonymizedName(), closure.username());
        String cleanupId = UUID.randomUUID().toString();
        cleanupMapper.enqueue(cleanupId, closure.username(), closure.anonymizedName(), closure.userId());
        events.publishEvent(new DeletionCleanupWorker.CleanupRequested(cleanupId));
    }

    /**
     * Login name used for anonymized community content: del_ + 15 hex chars + reuse count.
     */
    private String nextAnonymizedName(String username) {
        String hashed = StringEncryptUtils.sha1HexString(username).substring(0, 15);
        Integer count = userMapper.selectDeletedUserCount(ANONYMIZED_PREFIX + hashed);
        int suffix = count == null ? 0 : count;
        return ANONYMIZED_PREFIX + hashed + "_" + suffix;
    }
}
