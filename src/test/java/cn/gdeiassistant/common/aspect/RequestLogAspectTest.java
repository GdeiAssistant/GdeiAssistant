package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.core.charge.pojo.dto.ChargeRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestLogAspectTest {

    private RequestLogAspect requestLogAspect;

    @BeforeEach
    void setUp() {
        requestLogAspect = new RequestLogAspect();
    }

    @Test
    void shouldRedactDirectSensitiveParameterNames() {
        assertEquals("[REDACTED]", serialize("password", "Passw0rd!"));
        assertEquals("[REDACTED]", serialize("pwd", "Passw0rd!"));
        assertEquals("[REDACTED]", serialize("passwd", "Passw0rd!"));
        assertEquals("[REDACTED]", serialize("captcha", "246810"));
        assertEquals("[REDACTED]", serialize("verificationCode", "135790"));
        assertEquals("[REDACTED]", serialize("smsCode", "864209"));
        assertEquals("[REDACTED]", serialize("jwt", "jwt_abcdefghijklmnopqrstuvwxyz"));
        assertEquals("[REDACTED]", serialize("hmac", "abcdef1234567890fedcba"));
        assertEquals("[REDACTED]", serialize("token", "tok_abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    void shouldMaskNestedSensitiveFieldsAndKeepNonSensitiveFields() {
        String rawPassword = "Passw0rd!";
        String rawPhone = "13812345678";
        String rawEmail = "campus@example.test";
        String rawAddress = "Dorm-A-302";
        String rawPickupCode = "PK-778899";
        String rawVerificationCode = "889900";
        String rawSessionToken = "session_abcdefghijklmnopqrstuvwxyz123456";

        Map<String, Object> payload = Map.of(
                "note", "keep-me",
                "password", rawPassword,
                "phone", rawPhone,
                "contactEmail", rawEmail,
                "address", rawAddress,
                "pickupCode", rawPickupCode,
                "verificationCode", rawVerificationCode,
                "nested", Map.of(
                        "sessionToken", rawSessionToken,
                        "nickname", "tester"
                )
        );

        String serialized = serialize("payload", payload);

        assertTrue(serialized.contains("keep-me"));
        assertTrue(serialized.contains("tester"));
        assertTrue(serialized.contains("[REDACTED]"));
        assertFalse(serialized.contains(rawPassword));
        assertFalse(serialized.contains(rawPhone));
        assertFalse(serialized.contains(rawEmail));
        assertFalse(serialized.contains(rawAddress));
        assertFalse(serialized.contains(rawPickupCode));
        assertFalse(serialized.contains(rawVerificationCode));
        assertFalse(serialized.contains(rawSessionToken));
    }

    @Test
    void shouldMaskLongTokenLikeStringsEvenWithGenericFieldName() {
        String rawToken = "tok_abcdefghijklmnopqrstuvwxyz_1234567890";

        String serialized = serialize("payload", rawToken);

        assertTrue(serialized.contains(rawToken.substring(0, 6)));
        assertTrue(serialized.contains(rawToken.substring(rawToken.length() - 4)));
        assertTrue(serialized.contains("..."));
        assertFalse(serialized.contains(rawToken));
    }

    @Test
    void shouldNotSerializeLegacyChargeSignatureFields() {
        ChargeRequestDTO request = new ChargeRequestDTO();
        request.setAmount(50);
        request.setPassword("synthetic-charge-password");
        request.setHmac("synthetic-legacy-hmac");
        request.setTimestamp("1234567890");

        String serialized = serialize("requestParams", request);

        assertTrue(serialized.contains("\"amount\":\"50\""));
        assertFalse(serialized.contains("synthetic-charge-password"));
        assertFalse(serialized.contains("synthetic-legacy-hmac"));
        assertFalse(serialized.contains("1234567890"));
        assertFalse(serialized.contains("password"));
        assertFalse(serialized.contains("hmac"));
        assertFalse(serialized.contains("timestamp"));
    }

    @Test
    void shouldHandleNullEmptyMapAndPlainStringWithoutThrowing() {
        assertEquals("null", serialize("payload", null));
        assertEquals("{}", serialize("payload", Map.of()));

        String plain = assertDoesNotThrow(() -> serialize("payload", "{broken-json"));
        assertTrue(plain.contains("{broken-json"));
        assertFalse(plain.contains("[serialization-error]"));
    }

    private String serialize(String paramName, Object value) {
        return ReflectionTestUtils.invokeMethod(requestLogAspect, "serializeForLog", paramName, value);
    }
}
