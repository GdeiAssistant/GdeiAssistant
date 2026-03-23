package cn.gdeiassistant.core.userProfile.controller.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileLocationValidatorTest {

    private final ProfileLocationValidator validator = new ProfileLocationValidator();

    @Test
    void invalidRegionCode_returnsFailure() {
        ProfileLocationValidator.ValidationResult result = validator.validate("NONEXISTENT", null, null);
        assertFalse(result.isValid());
        assertEquals("不合法的国家/地区代码", result.getErrorMessage());
    }

    @Test
    void invalidRegionCode_resultFieldsAreNull() {
        ProfileLocationValidator.ValidationResult result = validator.validate("NONEXISTENT", null, null);
        assertFalse(result.isValid());
        assertNull(result.getRegion());
        assertNull(result.getState());
        assertNull(result.getCity());
    }

    @Test
    void validRegionStateCity_returnsSuccess() {
        // CN = China, State 12 = Tianjin, City 1 = Heping
        ProfileLocationValidator.ValidationResult result = validator.validate("CN", "12", "1");
        assertTrue(result.isValid(), "Expected valid result for CN/12/1");
        assertNotNull(result.getRegion());
        assertNotNull(result.getState());
        assertNotNull(result.getCity());
    }
}
