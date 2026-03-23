package cn.gdeiassistant.core.userProfile.controller.support;

import cn.gdeiassistant.common.pojo.Entity.City;
import cn.gdeiassistant.common.pojo.Entity.State;
import cn.gdeiassistant.common.tools.Utils.LocationUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Validates region/state/city codes against the location dictionary.
 * Extracted from ProfileController to keep location validation rules
 * in a single, testable place.
 */
@Component
public class ProfileLocationValidator {

    /**
     * Result of a location validation attempt.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final String region;
        private final String state;
        private final String city;

        private ValidationResult(boolean valid, String errorMessage, String region, String state, String city) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.region = region;
            this.state = state;
            this.city = city;
        }

        static ValidationResult success(String region, String state, String city) {
            return new ValidationResult(true, null, region, state, city);
        }

        static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage, null, null, null);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public String getRegion() { return region; }
        public String getState() { return state; }
        public String getCity() { return city; }
    }

    /**
     * Validate region/state/city codes.
     * Returns a ValidationResult with the resolved (possibly null) state/city
     * when the region has no sub-divisions, or the exact codes when they exist.
     */
    public ValidationResult validate(String region, String state, String city) {
        if (!LocationUtils.getRegionMap().containsKey(region)) {
            return ValidationResult.failure("不合法的国家/地区代码");
        }
        Map<String, State> stateMap = LocationUtils.getRegionMap().get(region).getStateMap();
        if (stateMap == null || stateMap.isEmpty()) {
            return ValidationResult.success(region, null, null);
        }
        if (state == null || !stateMap.containsKey(state)) {
            return ValidationResult.failure("不合法的省/州代码");
        }
        Map<String, City> cityMap = stateMap.get(state).getCityMap();
        if (cityMap == null || cityMap.isEmpty()) {
            return ValidationResult.success(region, state, null);
        }
        if (city == null || !cityMap.containsKey(city)) {
            return ValidationResult.failure("不合法的市/直辖市代码");
        }
        return ValidationResult.success(region, state, city);
    }
}
