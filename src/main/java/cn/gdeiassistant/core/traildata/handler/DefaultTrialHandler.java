package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.exception.CommonException.TestAccountException;
import cn.gdeiassistant.common.pojo.Entity.Book;
import cn.gdeiassistant.common.pojo.Entity.Card;
import cn.gdeiassistant.common.pojo.Entity.CardInfo;
import cn.gdeiassistant.common.pojo.Entity.Collection;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import cn.gdeiassistant.common.pojo.Entity.SpareRoom;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.cardquery.pojo.CardQuery;
import cn.gdeiassistant.core.cardquery.pojo.CardQueryResult;
import cn.gdeiassistant.common.pojo.Document.CardTestDocument;
import cn.gdeiassistant.core.book.repository.BookDao;
import cn.gdeiassistant.core.card.repository.CardDao;
import cn.gdeiassistant.core.collection.repository.CollectionDao;
import cn.gdeiassistant.core.spare.repository.SpareDao;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用样板间处理器：兜底处理所有未显式声明的 @TrialData 模块。
 * 对测试账号统一返回“测试账号不支持该功能”。
 */
@Component
public class DefaultTrialHandler extends AbstractTrialModuleHandler {

    public static final String DEFAULT_KEY = "__default__";

    @Override
    public String getModuleKey() {
        return DEFAULT_KEY;
    }

    @Override
    public DataJsonResult<?> handle(String sessionId,
                                    HttpServletRequest request,
                                    TrialData trialData,
                                    ProceedingJoinPoint joinPoint) throws Throwable {
        // 默认行为：样板间未显式支持的模块一律返回“测试账号不支持该功能”
        return buildError(TrialErrorCode.NOT_SUPPORTED);
    }
}

