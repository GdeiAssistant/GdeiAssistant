package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.constant.ObservabilityConstants;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(3)
public class RequestLogAspect {

    private final Logger logger = LoggerFactory.getLogger(RequestLogAspect.class);
    private static final Set<String> HTTP_METHODS = Set.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS", "TRACE");

    @Autowired
    private ObservabilityConstants observabilityConstants;

    @Autowired
    private MeterRegistry meterRegistry;

    @Pointcut("@annotation(cn.gdeiassistant.common.annotation.RequestLogPersistence)")
    public void requestAction() {
    }

    @AfterReturning("requestAction()")
    public void restSaveQueryLog(JoinPoint joinPoint) {
        // Payloads and client headers may contain arbitrary private data. Log only code identity.
        logger.info("RequestLog - handler:{}", handler(joinPoint));
    }

    @Pointcut("execution(* cn.gdeiassistant..controller..*(..))")
    public void allControllerMethods() {
    }

    /** Records controller outcomes without logging request bodies, identifiers or raw URLs. */
    @Around("allControllerMethods()")
    public Object logRequestCorrelation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = resolveRequest(joinPoint);
        String requestId = request != null ? String.valueOf(request.getAttribute("requestId")) : "?";
        String method = request != null && HTTP_METHODS.contains(request.getMethod()) ? request.getMethod() : "?";
        Object routeAttribute = request != null
                ? request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) : null;
        String route = routeAttribute != null ? routeAttribute.toString() : "?";
        String handler = handler(joinPoint);
        long start = System.nanoTime();
        String status = "OK";
        String exceptionType = "-";
        try {
            Object result = joinPoint.proceed();
            Object body = result;
            if (result instanceof ResponseEntity<?> response) {
                body = response.getBody();
                if (response.getStatusCode().isError()) {
                    status = "FAIL";
                }
            }
            if (body instanceof JsonResult json && Boolean.FALSE.equals(json.isSuccess())) {
                status = "FAIL";
            }
            return result;
        } catch (Throwable failure) {
            status = "FAIL";
            exceptionType = failure.getClass().getSimpleName();
            throw failure;
        } finally {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            logger.info("CorrelationLog - rid:{} | {} {} | handler:{} | status:{} | exceptionType:{} | elapsed:{}ms",
                    requestId, method, route, handler, status, exceptionType, elapsed);
            if (elapsed > observabilityConstants.getSlowRequestThresholdMs()) {
                logger.warn("SlowRequest - rid:{} | {} {} | handler:{} | elapsed:{}ms (threshold:{}ms)",
                        requestId, method, route, handler, elapsed, observabilityConstants.getSlowRequestThresholdMs());
                // Code identities are bounded; raw URLs and request IDs must never be metric labels.
                meterRegistry.counter("http.requests.slow", Tags.of("method", method, "handler", handler)).increment();
            }
        }
    }

    private String handler(JoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringType().getSimpleName() + '.' + joinPoint.getSignature().getName();
    }

    private HttpServletRequest resolveRequest(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest request) {
                return request;
            }
        }
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs
                ? attrs.getRequest() : null;
    }
}
