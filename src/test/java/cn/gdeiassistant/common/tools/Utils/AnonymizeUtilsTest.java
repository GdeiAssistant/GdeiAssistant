package cn.gdeiassistant.common.tools.Utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnonymizeUtilsTest {

    @Test
    void shouldMaskPhoneWithoutLeakingOriginalDigits() {
        String raw = "13812345678";

        String masked = AnonymizeUtils.maskPhone(raw);

        assertEquals("138****5678", masked);
        assertNotEquals(raw, masked);
        assertFalse(masked.contains("12345678"));
    }

    @Test
    void shouldHandleBlankNullAndShortPhoneValues() {
        assertNull(AnonymizeUtils.maskPhone(null));
        assertEquals("", AnonymizeUtils.maskPhone(""));
        assertEquals("***", AnonymizeUtils.maskPhone("1234"));
        assertDoesNotThrow(() -> AnonymizeUtils.maskPhone("1234"));
    }

    @Test
    void shouldMaskEmailWithoutLeakingOriginalValue() {
        String raw = "abc@example.com";

        String masked = AnonymizeUtils.maskEmail(raw);

        assertEquals("a***@example.com", masked);
        assertNotEquals(raw, masked);
        assertFalse(masked.contains(raw));
    }

    @Test
    void shouldHandleInvalidBlankAndNullEmail() {
        assertNull(AnonymizeUtils.maskEmail(null));
        assertEquals("", AnonymizeUtils.maskEmail(""));

        String raw = "not-an-email";
        String masked = assertDoesNotThrow(() -> AnonymizeUtils.maskEmail(raw));
        assertNotEquals(raw, masked);
        assertFalse(masked.contains(raw));
    }

    @Test
    void shouldMaskUsernameAndSanitizeDeletedPrefix() {
        assertEquals("已注销用户", AnonymizeUtils.sanitizeUsername("del_virtual_user"));

        String raw = "campusUser2026";
        String masked = AnonymizeUtils.maskUsername(raw);

        assertTrue(masked.startsWith("camp"));
        assertTrue(masked.endsWith("2026"));
        assertNotEquals(raw, masked);
        assertFalse(masked.contains(raw));
    }

    @Test
    void shouldMaskShortUsernameWithoutExposingOriginalValue() {
        assertNull(AnonymizeUtils.maskUsername(null));

        String shortName = "ab";
        String maskedShort = AnonymizeUtils.maskUsername(shortName);
        assertEquals("*", maskedShort);
        assertNotEquals(shortName, maskedShort);

        String compactName = "abc";
        String maskedCompact = AnonymizeUtils.maskUsername(compactName);
        assertNotEquals(compactName, maskedCompact);
        assertTrue(maskedCompact.contains("*"));
    }

    @Test
    void shouldMaskLongTokenWithoutLeakingOriginalValue() {
        String raw = "tok_abcdefghijklmnopqrstuvwxyz_1234567890";

        String masked = AnonymizeUtils.maskToken(raw);

        assertTrue(masked.startsWith(raw.substring(0, 6)));
        assertTrue(masked.endsWith(raw.substring(raw.length() - 4)));
        assertTrue(masked.contains("..."));
        assertNotEquals(raw, masked);
        assertFalse(masked.contains(raw));
    }

    @Test
    void shouldHandleShortBlankAndNullToken() {
        assertNull(AnonymizeUtils.maskToken(null));
        assertEquals("", AnonymizeUtils.maskToken(""));
        assertEquals("***", AnonymizeUtils.maskToken("short"));
        assertDoesNotThrow(() -> AnonymizeUtils.maskToken("short"));
    }

    @Test
    void shouldMaskAddressAndPickupLikeIdentifier() {
        String rawAddress = "DormA-BlockB-302";
        String maskedAddress = AnonymizeUtils.maskAddress(rawAddress);
        assertNotEquals(rawAddress, maskedAddress);
        assertFalse(maskedAddress.contains(rawAddress));
        assertTrue(maskedAddress.endsWith("***"));

        String rawPickupCode = "PK123456";
        String maskedPickupCode = AnonymizeUtils.maskIdentifier(rawPickupCode, 2, 2);
        assertNotEquals(rawPickupCode, maskedPickupCode);
        assertFalse(maskedPickupCode.contains(rawPickupCode));
        assertTrue(maskedPickupCode.startsWith("PK"));
        assertTrue(maskedPickupCode.endsWith("56"));
    }

    @Test
    void shouldHandleNullBlankAndShortAddressValues() {
        assertNull(AnonymizeUtils.maskAddress(null));
        assertEquals("", AnonymizeUtils.maskAddress(""));
        assertNotEquals("A", AnonymizeUtils.maskAddress("A"));
        assertTrue(AnonymizeUtils.maskAddress("A").contains("***"));
        assertDoesNotThrow(() -> AnonymizeUtils.maskAddress("A"));
    }
}
