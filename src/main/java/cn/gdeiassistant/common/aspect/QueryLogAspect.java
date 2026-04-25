package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.constant.ObservabilityConstants;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Aspect
@Component
@Order(4)
public class QueryLogAspect {

    private final Logger logger = LoggerFactory.getLogger(QueryLogAspect.class);

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ObservabilityConstants observabilityConstants;

    @Autowired
    private MeterRegistry meterRegistry;

    @Pointcut("@annotation(cn.gdeiassistant.common.annotation.QueryLogPersistence)")
    public void QueryAction() {

    }

    @Pointcut("@annotation(cn.gdeiassistant.common.annotation.RestQueryLogPersistence)")
    public void RestQueryAction() {

    }

    @Pointcut("execution(* cn.gdeiassistant.core.secret.controller.SecretController.*(..)) || "
            + "execution(* cn.gdeiassistant.core.topic.controller.TopicController.*(..)) || "
            + "execution(* cn.gdeiassistant.core.express.controller.ExpressController.*(..)) || "
            + "execution(* cn.gdeiassistant.core.marketplace.controller.MarketplaceController.*(..)) || "
            + "execution(* cn.gdeiassistant.core.lostandfound.controller.LostAndFoundController.*(..)) || "
            + "execution(* cn.gdeiassistant.core.delivery.controller.DeliveryController.*(..)) || "
            + "execution(* cn.gdeiassistant.core.dating.controller.DatingController.*(..)) || "
            + "execution(* cn.gdeiassistant.core.photograph.controller.PhotographController.*(..))")
    public void CommunityQueryAction() {

    }

    @Around("RestQueryAction()")
    public Object RestSaveQueryLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String username = AnonymizeUtils.maskUsername((Optional.ofNullable((User) request.getAttribute("user"))
                .orElse(new User("unknown"))).getUsername());
        String dateTime = dateFormat.format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        String functionName = joinPoint.getSignature().getName();
        switch (functionName) {
            //成绩查询
            case "gradequery":
                logger.info("用户{}于{}查询了成绩信息", username, dateTime);
                break;

            //课表查询
            case "schedulequery":
                logger.info("用户{}于{}查询了课表信息", username, dateTime);
                break;

            //消费记录查询
            case "cardquery":
                logger.info("用户{}于{}查询了消费记录信息", username, dateTime);
                break;
        }

        recordSlowQueryIfNeeded(functionName, elapsed, "rest");

        return result;
    }

    @Around("QueryAction()")
    public Object SaveQueryLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        Object[] args = joinPoint.getArgs();
        String username = AnonymizeUtils.maskUsername(Optional.ofNullable((String) WebUtils.getSessionAttribute((HttpServletRequest) args[0]
                , "username")).orElse("unknown"));
        String dateTime = dateFormat.format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        String functionName = joinPoint.getSignature().getName();
        switch (functionName) {
            //成绩查询
            case "gradequery":
                logger.info("用户{}于{}查询了成绩信息", username, dateTime);
                break;

            //课表查询
            case "schedulequery":
                logger.info("用户{}于{}查询了课表信息", username, dateTime);
                break;

            //消费记录查询
            case "cardquery":
                logger.info("用户{}于{}查询了消费记录信息", username, dateTime);
                break;

            //校园卡信息查询
            case "cardInfoQuery":
                logger.info("用户{}于{}查询了校园卡基本信息", username, dateTime);
                break;

            //校园卡挂失
            case "cardLost":
                logger.info("用户{}于{}进行了校园卡挂失操作", username, dateTime);
                break;

            //查询空课室信息
            case "querySpareRoomList":
                logger.info("用户{}于{}查询了空课室信息", username, dateTime);
                break;
        }

        recordSlowQueryIfNeeded(functionName, elapsed, "session");

        return result;
    }

    @Around("CommunityQueryAction()")
    public Object CommunityQueryLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        String methodName = joinPoint.getSignature().toShortString();
        logger.info("Community query executed: {} in {}ms", methodName, elapsed);

        recordSlowQueryIfNeeded(methodName, elapsed, "community");

        return result;
    }

    private void recordSlowQueryIfNeeded(String operation, long elapsedMs, String category) {
        long threshold = observabilityConstants.getSlowQueryThresholdMs();
        if (elapsedMs > threshold) {
            logger.warn("SlowQuery - operation:{} | elapsed:{}ms (threshold:{}ms) | category:{}",
                    operation, elapsedMs, threshold, category);
            meterRegistry.counter("query.slow",
                    Tags.of("operation", operation, "category", category)).increment();
        }
    }
}
