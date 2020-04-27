package edu.gdei.gdeiassistant.Aspect;

import edu.gdei.gdeiassistant.Pojo.Entity.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
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

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.QueryLogPersistence)")
    public void QueryAction() {

    }

    @Pointcut("@annotation(edu.gdei.gdeiassistant.Annotation.RestQueryLogPersistence)")
    public void RestQueryAction() {

    }

    @AfterReturning("RestQueryAction()")
    public void RestSaveQueryLog(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        String username = (Optional.ofNullable((User) request.getAttribute("user"))
                .orElse(new User("unknown"))).getUsername();
        String dateTime = dateFormat.format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        String functionName = joinPoint.getSignature().getName();
        switch (functionName) {
            //成绩查询
            case "GradeQuery":
                logger.info("用户{}于{}查询了成绩信息", username, dateTime);
                break;

            //课表查询
            case "ScheduleQuery":
                logger.info("用户{}于{}查询了课表信息", username, dateTime);
                break;

            //消费记录查询
            case "CardQuery":
                logger.info("用户{}于{}查询了消费记录信息", username, dateTime);
                break;
        }
    }

    @AfterReturning("QueryAction()")
    public void SaveQueryLog(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String username = Optional.ofNullable((String) WebUtils.getSessionAttribute((HttpServletRequest) args[0]
                , "username")).orElse("unknown");
        String dateTime = dateFormat.format(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        String functionName = joinPoint.getSignature().getName();
        switch (functionName) {
            //成绩查询
            case "GradeQuery":
                logger.info("用户{}于{}查询了成绩信息", username, dateTime);
                break;

            //课表查询
            case "ScheduleQuery":
                logger.info("用户{}于{}查询了课表信息", username, dateTime);
                break;

            //消费记录查询
            case "CardQuery":
                logger.info("用户{}于{}查询了消费记录信息", username, dateTime);
                break;

            //校园卡信息查询
            case "CardInfoQuery":
                logger.info("用户{}于{}查询了校园卡基本信息", username, dateTime);
                break;

            //校园卡挂失
            case "CardLost":
                logger.info("用户{}于{}进行了校园卡挂失操作", username, dateTime);
                break;

            //查询空课室信息
            case "QuerySpareRoomList":
                logger.info("用户{}于{}查询了空课室信息", username, dateTime);
                break;
        }
    }
}
