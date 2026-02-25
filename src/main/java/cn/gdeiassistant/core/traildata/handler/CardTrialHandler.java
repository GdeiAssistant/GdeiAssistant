package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Entity.Card;
import cn.gdeiassistant.common.pojo.Entity.CardInfo;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Document.CardTestDocument;
import cn.gdeiassistant.core.cardquery.pojo.CardQuery;
import cn.gdeiassistant.core.cardquery.pojo.CardQueryResult;
import cn.gdeiassistant.core.card.repository.CardDao;
import cn.gdeiassistant.core.trailData.service.TrialDataService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 校园卡样板间处理器：card、cardinfo、cardlost。
 */
@Component
public class CardTrialHandler extends AbstractTrialModuleHandler {

    @Autowired(required = false)
    private CardDao cardDao;

    @Override
    public String getModuleKey() {
        return "card";
    }

    @Override
    public java.util.List<String> getSupportedModules() {
        return java.util.Arrays.asList("card", "cardinfo", "cardlost");
    }

    @Override
    public DataJsonResult<?> handle(String sessionId,
                                    HttpServletRequest request,
                                    TrialData trialData,
                                    ProceedingJoinPoint joinPoint) {
        cn.gdeiassistant.common.pojo.Entity.User user =
                (cn.gdeiassistant.common.pojo.Entity.User) request.getAttribute("user");
        String username = user != null ? user.getUsername() : null;
        String module = trialData.value();
        switch (module) {
            case "card":
                return handleCardQuery(request, username);
            case "cardinfo":
                return handleCardInfo(username);
            case "cardlost":
                return buildError(TrialErrorCode.NOT_SUPPORTED);
            default:
                return null;
        }
    }

    private DataJsonResult<?> handleCardInfo(String username) {
        return loadFromTrialOrFallback("card", () -> {
            if (cardDao == null) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            CardTestDocument doc = cardDao.queryCardDocumentByUsername(username);
            CardInfo info = doc != null ? doc.getCardInfo() : null;
            if (info == null) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            CardQueryResult result = new CardQueryResult();
            result.setCardInfo(info);
            result.setCardList(Collections.<Card>emptyList());
            return new DataJsonResult<>(true, result);
        });
    }

    private DataJsonResult<?> handleCardQuery(HttpServletRequest req, String username) {
        return loadFromTrialOrFallback("card", () -> {
            if (cardDao == null) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            CardTestDocument doc = cardDao.queryCardDocumentByUsername(username);
            CardInfo info = doc != null ? doc.getCardInfo() : null;
            List<Card> allRecords = (doc != null && doc.getCardRecordList() != null)
                    ? doc.getCardRecordList()
                    : Collections.<Card>emptyList();

            // 按查询条件（year, month, date）做一个简单的基于日期前缀的过滤
            String yearParam = req.getParameter("year");
            String monthParam = req.getParameter("month");
            String dateParam = req.getParameter("date");
            List<Card> filtered = allRecords;
            if (yearParam != null && monthParam != null && dateParam != null) {
                String mm = monthParam.length() == 1 ? "0" + monthParam : monthParam;
                String dd = dateParam.length() == 1 ? "0" + dateParam : dateParam;
                String prefix = yearParam + "-" + mm + "-" + dd;
                filtered = allRecords.stream()
                        .filter(c -> c.getTradeTime() != null && c.getTradeTime().startsWith(prefix))
                        .collect(java.util.stream.Collectors.toList());
            }

            CardQueryResult result = new CardQueryResult();
            CardQuery cq = new CardQuery();
            try {
                if (yearParam != null) {
                    cq.setYear(Integer.parseInt(yearParam));
                }
                if (monthParam != null) {
                    cq.setMonth(Integer.parseInt(monthParam));
                }
                if (dateParam != null) {
                    cq.setDate(Integer.parseInt(dateParam));
                }
            } catch (NumberFormatException ignore) {
            }
            result.setCardQuery(cq);
            result.setCardInfo(info);
            result.setCardList(filtered);
            return new DataJsonResult<>(true, result);
        });
    }
}

