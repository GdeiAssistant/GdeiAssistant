package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Entity.SpareRoom;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.spare.repository.SpareDao;
import cn.gdeiassistant.core.trailData.service.TrialDataService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 空课室查询样板间处理器：spare。
 */
@Component
public class SpareTrialHandler extends AbstractTrialModuleHandler {

    @Autowired(required = false)
    private SpareDao spareDao;

    @Override
    public String getModuleKey() {
        return "spare";
    }

    @Override
    public DataJsonResult<?> handle(String sessionId,
                                    HttpServletRequest request,
                                    TrialData trialData,
                                    ProceedingJoinPoint joinPoint) {
        cn.gdeiassistant.common.pojo.Entity.User user =
                (cn.gdeiassistant.common.pojo.Entity.User) request.getAttribute("user");
        String username = user != null ? user.getUsername() : null;
        return loadFromTrialOrFallback("spare", () -> {
            if (spareDao == null) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            List<SpareRoom> list = spareDao.querySpareListByUsername(username);
            if (list == null || list.isEmpty()) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            return new DataJsonResult<>(true, list);
        });
    }
}

