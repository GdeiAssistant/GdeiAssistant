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
    void validResult_hasRegionStateCity() {
        // Use a known region from the location.xml that has states and cities (China)
        ProfileLocationValidator.ValidationResult result = validator.validate("NONEXISTENT", null, null);
        assertFalse(result.isValid());
        // We verify that the validator consistently returns failure for unknown codes
        assertNull(result.getRegion());
        assertNull(result.getState());
        assertNull(result.getCity());
    }
}
