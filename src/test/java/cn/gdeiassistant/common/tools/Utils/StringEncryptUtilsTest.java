package cn.gdeiassistant.common.tools.Utils;

import cn.gdeiassistant.common.pojo.Encryption.AESEncryptConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StringEncryptUtilsTest {

    private final StringEncryptUtils encryptUtils = new StringEncryptUtils();

    @BeforeEach
    void setUp() {
        AESEncryptConfig config = new AESEncryptConfig();
        config.setPrivateKey("test-only-private-key");
        encryptUtils.setEncryptConfig(config);
    }

    @AfterEach
    void tearDown() {
        encryptUtils.setEncryptConfig(null);
    }

    @Test
    void roundTripWorksAcrossIndependentCalls() throws Exception {
        String encrypted = StringEncryptUtils.encryptString("campus-password");

        assertTrue(encrypted.startsWith("v1:"));
        assertEquals("campus-password", StringEncryptUtils.decryptString(encrypted));
    }

    @Test
    void emptyStringRoundTripWorks() throws Exception {
        assertEquals("", StringEncryptUtils.decryptString(StringEncryptUtils.encryptString("")));
    }

    @Test
    void encryptionsUseFreshIv() throws Exception {
        assertNotEquals(
                StringEncryptUtils.encryptString("same-value"),
                StringEncryptUtils.encryptString("same-value")
        );
    }

    @Test
    void tamperingIsRejected() throws Exception {
        String encrypted = StringEncryptUtils.encryptString("campus-password");
        byte[] payload = Base64.getUrlDecoder().decode(encrypted.substring("v1:".length()));
        payload[payload.length - 1] ^= 1;
        String tampered = "v1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(payload);

        assertThrows(GeneralSecurityException.class,
                () -> StringEncryptUtils.decryptString(tampered));
    }

    @Test
    void legacyValuesAreRejectedInsteadOfReturningNull() {
        assertThrows(GeneralSecurityException.class,
                () -> StringEncryptUtils.decryptString("legacy-ciphertext"));
    }
}
