package cn.gdeiassistant.core.grade.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import org.springframework.stereotype.Component;

/**
 * Grade cache is derived data from the academic system and is removed after commit.
 */
@Component
public class GradeAccountContributor implements AccountLifecycleContributor {

    private final GradeDao gradeDao;

    public GradeAccountContributor(GradeDao gradeDao) {
        this.gradeDao = gradeDao;
    }

    @Override
    public void cleanupAfterCommit(AccountClosure closure) {
        gradeDao.removeGrade(closure.username());
    }
}
