package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.constant.ObservabilityConstants;
import cn.gdeiassistant.common.pojo.Entity.RequestSecurity;
import cn.gdeiassistant.common.pojo.Entity.RequestValidation;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import com.alibaba.fastjson2.JSON;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

@Aspect
@Component
@Order(3)
public class RequestLogAspect {

    private final Logger logger = LoggerFactory.getLogger(RequestLogAspect.class);

    /** Maximum characters for any single serialized parameter value in the log line. */
    private static final int MAX_PARAM_SERIALIZED_LENGTH = 512;

    /** Sensitive parameter names that must never be logged. */
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
            "password", "token", "secret", "credential", "accesskey"
    );

    @Autowired
    private ObservabilityConstants observabilityConstants;

    @Autowired
    private MeterRegistry meterRegistry;

    // ----------------------------------------------------------------
    // Existing @RequestLogPersistence behaviour (unchanged)
    // ----------------------------------------------------------------

    @Pointcut("@annotation(cn.gdeiassistant.common.annotation.RequestLogPersistence)")
    public void RequestAction() {

    }

    @AfterReturning("RequestAction()")
    public void RestSaveQueryLog(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] parameterName = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        HttpServletRequest request = (HttpServletRequest) args[0];
        StringBuilder stringBuilder = new StringBuilder("RequestLog - ");
        //记录请求的方法
        stringBuilder.append("RequestName:").append(joinPoint.getSignature().getDeclaringTypeName()).append(" - ").append(joinPoint.getSignature().getName());
        //记录请求时间
        stringBuilder.append(" . RequestTime:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        //记录请求头信息
        stringBuilder.append(" . RequestHeaders: ");
        stringBuilder.append(StringUtils.isBlank(request.getHeader("User-Agent")) ? "" : "User-Agent:" + request.getHeader("User-Agent") + " , ");
        stringBuilder.append(StringUtils.isBlank(request.getHeader("X-Client-Type")) ? "" : "X-Client-Type:" + request.getHeader("X-Client-Type") + " , ");
        stringBuilder.append(StringUtils.isBlank(request.getHeader("Version-Code")) ? "" : "Version-Code:" + request.getHeader("Version-Code"));
        //记录非敏感请求参数信息
        stringBuilder.append(". RequestParameters: ");
        for (int i = 1; i < args.length; i++) {
            if (args[i] != null && !(parameterName[i].contains("password") || parameterName[i].contains("token"))) {
                if (i != 1) {
                    stringBuilder.append(" , ");
                }
                if (args[i] instanceof RequestValidation || args[i] instanceof RequestSecurity) {
                    stringBuilder.append(parameterName[i]).append(":[REDACTED]");
                } else {
                    stringBuilder.append(parameterName[i])
                            .append(":").append(JSON.toJSONString(args[i]));
                }
            }
        }
        stringBuilder.append(" . ");
        //保存请求信息到日志
        logger.info(stringBuilder.toString());
    }

    // ----------------------------------------------------------------
    // Broad correlation / latency logging for ALL controller methods
    // ----------------------------------------------------------------

    @Pointcut("execution(* cn.gdeiassistant..controller..*(..))")
    public void allControllerMethods() {
    }

    /**
     * Logs request correlation ID, HTTP method, path, client IP, parameters
     * (with sensitive redaction and size cap), elapsed time and success/failure
     * for every controller method invocation. Emits a slow-request counter when
     * the elapsed time exceeds the configured threshold.
     */
    @Around("allControllerMethods()")
    public Object logRequestCorrelation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = resolveRequest(joinPoint);

        String requestId = "?";
        String method = "?";
        String path = "?";
        String clientIp = "?";

        if (request != null) {
            Object idAttr = request.getAttribute("requestId");
            if (idAttr != null) {
                requestId = idAttr.toString();
            }
            method = request.getMethod();
            path = request.getRequestURI();
            clientIp = resolveClientIp(request);
        }

        long start = System.currentTimeMillis();
        boolean success = true;
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            success = false;
            throw t;
        } finally {
            long elapsed = System.currentTimeMillis() - start;

            // Build log line
            StringBuilder sb = new StringBuilder("CorrelationLog - ");
            sb.append("rid:").append(requestId);
            sb.append(" | ").append(method).append(' ').append(path);
            sb.append(" | ip:").append(clientIp);
            sb.append(" | handler:").append(joinPoint.getSignature().getDeclaringType().getSimpleName())
              .append('.').append(joinPoint.getSignature().getName());

            // Log non-sensitive parameters (skip file/upload args and servlet types)
            appendSafeParameters(sb, joinPoint);

            sb.append(" | status:").append(success ? "OK" : "FAIL");
            sb.append(" | elapsed:").append(elapsed).append("ms");

            if (elapsed > observabilityConstants.getSlowRequestThresholdMs()) {
                // Use handler (ClassName.method) as the metric tag, NOT path.
                // Paths such as /api/ershou/keyword/{keyword}/... contain free-text user
                // input that would cause unbounded metric-label cardinality.
                // Handler names are a closed, bounded set derived from the codebase.
                String handler = joinPoint.getSignature().getDeclaringType().getSimpleName()
                        + '.' + joinPoint.getSignature().getName();
                logger.warn("SlowRequest - rid:{} | {} {} | handler:{} | elapsed:{}ms (threshold:{}ms)",
                        requestId, method, path, handler, elapsed, observabilityConstants.getSlowRequestThresholdMs());
                meterRegistry.counter("http.requests.slow",
                        Tags.of("method", method, "handler", handler)).increment();
            }

            logger.info(sb.toString());
        }
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /**
     * Resolves the current {@link HttpServletRequest} either from the join-point
     * arguments or from the {@link RequestContextHolder}.
     */
    private HttpServletRequest resolveRequest(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        // Fallback to RequestContextHolder
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // Take the first IP in the chain (original client)
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Appends non-sensitive, non-binary parameters to the log line.
     * Skips {@link MultipartFile}, {@link HttpServletRequest},
     * {@link HttpServletResponse}, and parameters whose name suggests
     * they contain secrets. Serialized values are capped at
     * {@link #MAX_PARAM_SERIALIZED_LENGTH} characters to prevent giant log lines.
     */
    private void appendSafeParameters(StringBuilder sb, ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] names;
        try {
            names = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        } catch (Exception e) {
            // Parameter names not available at runtime
            return;
        }
        if (names == null || names.length == 0) {
            return;
        }

        sb.append(" | params:{");
        boolean first = true;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String name = names[i];

            // Skip servlet infrastructure types and file uploads
            if (arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof MultipartFile[]) {
                continue;
            }

            // Redact sensitive parameters
            if (isSensitive(name)) {
                if (!first) sb.append(", ");
                sb.append(name).append(":[REDACTED]");
                first = false;
                continue;
            }

            // Redact RequestValidation / RequestSecurity
            if (arg instanceof RequestValidation || arg instanceof RequestSecurity) {
                if (!first) sb.append(", ");
                sb.append(name).append(":[REDACTED]");
                first = false;
                continue;
            }

            if (arg != null) {
                if (!first) sb.append(", ");
                try {
                    String serialized = JSON.toJSONString(arg);
                    if (serialized.length() > MAX_PARAM_SERIALIZED_LENGTH) {
                        serialized = serialized.substring(0, MAX_PARAM_SERIALIZED_LENGTH) + "...[truncated]";
                    }
                    sb.append(name).append(':').append(serialized);
                } catch (Exception e) {
                    sb.append(name).append(":[serialization-error]");
                }
                first = false;
            }
        }
        sb.append('}');
    }

    private boolean isSensitive(String paramName) {
        if (paramName == null) return false;
        String lower = paramName.toLowerCase();
        return SENSITIVE_PARAMS.stream().anyMatch(lower::contains);
    }

}
