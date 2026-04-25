package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RequestLogPersistence;
import cn.gdeiassistant.common.annotation.RestAuthentication;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import cn.gdeiassistant.core.charge.pojo.dto.ChargeRequestDTO;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeVO;
import cn.gdeiassistant.core.charge.service.ChargeService;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@RestController
public class ChargeRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ChargeRequestController.class);

    /** HMAC 时间戳有效窗口（秒） */
    private static final long HMAC_TIMESTAMP_TOLERANCE_SECONDS = 60;

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private UserMapper userMapper;

    @Value("${request.validate.token:}")
    private String hmacSecret;

    @RequestMapping(value = "/api/card/charge", method = RequestMethod.POST)
    @RestAuthentication
    @RequestLogPersistence
    @RateLimit(maxRequests = 3, windowSeconds = 60)
    public DataJsonResult<ChargeVO> ChargeRequest(HttpServletRequest request
            , @Validated ChargeRequestDTO requestParams) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");

        // 1. 设备绑定校验：充值操作必须携带设备标识
        String deviceId = request.getHeader("X-Device-ID");
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new ServerErrorException("充值请求必须携带设备标识");
        }

        // 2. HMAC 请求签名校验
        verifyHmac(requestParams);

        // 3. 二次验证用户密码
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (!java.security.MessageDigest.isEqual(
                userMapper.selectUser(user.getUsername()).getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                requestParams.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new PasswordIncorrectException("充值密码验证失败");
        }

        ChargeVO charge = chargeService.ChargeRequest(sessionId, requestParams.getAmount());
        chargeService.SaveChargeLog(sessionId, requestParams.getAmount());
        return new DataJsonResult<>(true, charge);
    }

    /**
     * 校验客户端 HMAC 签名，确保请求参数未被篡改。
     * 签名算法：HMAC-SHA256(secret, "amount={amount}&timestamp={timestamp}")
     */
    private void verifyHmac(ChargeRequestDTO requestParams) throws ServerErrorException {
        if (hmacSecret == null || hmacSecret.isBlank()) {
            throw new ServerErrorException("充值安全配置缺失，请联系管理员");
        }
        String clientHmac = requestParams.getHmac();
        String clientTimestamp = requestParams.getTimestamp();
        if (clientHmac == null || clientHmac.isBlank()
                || clientTimestamp == null || clientTimestamp.isBlank()) {
            throw new ServerErrorException("缺少请求签名参数");
        }
        // 校验时间戳
        try {
            long ts = Long.parseLong(clientTimestamp);
            long diff = Math.abs(Duration.between(Instant.ofEpochMilli(ts), Instant.now()).getSeconds());
            if (diff > HMAC_TIMESTAMP_TOLERANCE_SECONDS) {
                throw new ServerErrorException("请求签名已过期");
            }
        } catch (NumberFormatException e) {
            throw new ServerErrorException("请求时间戳格式错误");
        }
        // 计算并比对 HMAC
        String payload = "amount=" + requestParams.getAmount() + "&timestamp=" + clientTimestamp;
        String expectedHmac = hmacSha256(hmacSecret, payload);
        if (!expectedHmac.equals(clientHmac)) {
            logger.warn("充值 HMAC 校验失败: expected={}, actual={}", AnonymizeUtils.maskToken(expectedHmac), AnonymizeUtils.maskToken(clientHmac));
            throw new ServerErrorException("请求签名校验失败");
        }
    }

    private static String hmacSha256(String secret, String data) throws ServerErrorException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new ServerErrorException("HMAC 计算失败");
        }
    }
}
