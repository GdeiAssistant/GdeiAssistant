package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Entity.Book;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.book.repository.BookDao;
import cn.gdeiassistant.core.trailData.service.TrialDataService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 图书借阅样板间处理器：book、bookborrow、bookrenew。
 */
@Component
public class BookTrialHandler extends AbstractTrialModuleHandler {

    @Autowired(required = false)
    private BookDao bookDao;

    @Override
    public String getModuleKey() {
        return "book";
    }

    @Override
    public java.util.List<String> getSupportedModules() {
        return java.util.Arrays.asList("book", "bookborrow", "bookrenew");
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
            case "book":
            case "bookborrow":
                return handleBookBorrow(username);
            case "bookrenew":
                // 测试账号不支持续借，统一返回样板间错误码
                return buildError(TrialErrorCode.NOT_SUPPORTED);
            default:
                return null;
        }
    }

    private DataJsonResult<?> handleBookBorrow(String username) {
        return loadFromTrialOrFallback("book", () -> {
            if (bookDao == null) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            List<Book> list = bookDao.queryBookListByUsername(username);
            if (list == null || list.isEmpty()) {
                return buildError(TrialErrorCode.DATA_NOT_FOUND);
            }
            return new DataJsonResult<>(true, list);
        });
    }
}

