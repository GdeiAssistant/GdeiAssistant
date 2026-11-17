package cn.gdeiassistant.core.profile.service;

import cn.gdeiassistant.common.exception.databaseexception.UserNotExistException;
import cn.gdeiassistant.common.exception.tokenvalidexception.TokenExpiredException;
import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ProfilePatchService {

    public static final int NICKNAME_MAX_LENGTH = 32;
    public static final int INTRODUCTION_MAX_LENGTH = 80;
    public static final int MIN_YEAR = 1900;
    public static final ZoneId PROFILE_ZONE = ZoneId.of("Asia/Shanghai");

    private final ProfileMapper profiles;
    private final UserCertificateService certificates;
    private final ProfileLocationValidator locations;

    public ProfilePatchService(ProfileMapper profiles, UserCertificateService certificates, ProfileLocationValidator locations) {
        this.profiles = profiles;
        this.certificates = certificates;
        this.locations = locations;
    }

    public static class InvalidPatch extends IllegalArgumentException {
        private final Map<String, String> errors;

        public InvalidPatch(Map<String, String> errors) {
            super("请检查个人资料");
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }

    public interface BirthdayInput {
        Integer getYear();
        Integer getMonth();
        Integer getDate();
    }

    public interface LocationInput {
        String getRegion();
        String getState();
        String getCity();
    }

    public interface PatchInput {
        boolean hasField(String field);
        Map<String, Object> getUnknownFields();
        String getNickname();
        String getIntroduction();
        Integer getEnrollment();
        Integer getFaculty();
        String getMajor();
        BirthdayInput getBirthday();
        LocationInput getLocation();
        LocationInput getHometown();
    }

    public record TextValidation(boolean valid, String escaped, String errorMessage) {
    }

    public static TextValidation validateAndEscapeText(String raw, int maxLength, boolean allowEmpty, boolean treatNullAsEmpty) {
        if (raw == null) {
            return treatNullAsEmpty
                    ? new TextValidation(true, "", null)
                    : new TextValidation(false, null, "长度不合法");
        }
        String trimmed = raw.trim();
        if ((!allowEmpty && trimmed.isEmpty()) || trimmed.length() > maxLength) {
            return new TextValidation(false, null, "长度不合法");
        }
        String escaped = HtmlUtils.htmlEscape(trimmed);
        if (escaped.length() > maxLength) {
            return new TextValidation(false, null, "保存后的内容超过长度限制");
        }
        return new TextValidation(true, escaped, null);
    }

    public static LocalDate parseValidBirthday(Integer year, Integer month, Integer date) {
        if (year == null || month == null || date == null || year < MIN_YEAR) {
            return null;
        }
        try {
            LocalDate birthday = LocalDate.of(year, month, date);
            return birthday.isAfter(LocalDate.now(PROFILE_ZONE)) ? null : birthday;
        } catch (DateTimeException e) {
            return null;
        }
    }

    public static boolean isValidEnrollment(Integer year) {
        return year == null || (year >= MIN_YEAR && year <= LocalDate.now(PROFILE_ZONE).getYear());
    }

    public static boolean isValidFaculty(Integer faculty) {
        Map<Integer, String> facultyMap = UserProfileService.getFacultyMap();
        return faculty != null && facultyMap != null && facultyMap.containsKey(faculty);
    }

    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    public void save(String sessionId, PatchInput input) throws Exception {
        User user = certificates.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException(Messages.get("token.expired"));
        }
        ProfileEntity current = profiles.lockUserProfile(user.getUsername());
        if (current == null) {
            throw new UserNotExistException(Messages.get("profile.userMissing"));
        }
        if (input == null) {
            throw new InvalidPatch(Map.of("profile", "请求参数不合法"));
        }

        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, Object> patch = new LinkedHashMap<>();

        for (String unknownKey : input.getUnknownFields().keySet()) {
            errors.put(unknownKey, "不支持修改此字段");
        }

        if (input.hasField("nickname")) {
            TextValidation nickname = validateAndEscapeText(input.getNickname(), NICKNAME_MAX_LENGTH, false, false);
            if (!nickname.valid()) {
                errors.put("nickname", nickname.errorMessage());
            } else {
                patch.put("nickname", nickname.escaped());
            }
        }

        if (input.hasField("introduction")) {
            TextValidation introduction = validateAndEscapeText(input.getIntroduction(), INTRODUCTION_MAX_LENGTH, true, true);
            if (!introduction.valid()) {
                errors.put("introduction", introduction.errorMessage());
            } else {
                patch.put("introduction", introduction.escaped());
            }
        }

        if (input.hasField("birthday")) {
            BirthdayInput birthdayPatch = input.getBirthday();
            if (birthdayPatch == null) {
                patch.put("birthday", null);
            } else {
                LocalDate birthday = parseValidBirthday(birthdayPatch.getYear(), birthdayPatch.getMonth(), birthdayPatch.getDate());
                if (birthday == null) {
                    errors.put("birthday", "生日不合法");
                } else {
                    patch.put("birthday", java.sql.Date.valueOf(birthday));
                }
            }
        }

        if (input.hasField("enrollment")) {
            Integer year = input.getEnrollment();
            if (!isValidEnrollment(year)) {
                errors.put("enrollment", "入学年份不合法");
            } else {
                patch.put("enrollment", year);
            }
        }

        Integer faculty = current.getFaculty();
        if (input.hasField("faculty")) {
            faculty = input.getFaculty();
            if (!isValidFaculty(faculty)) {
                errors.put("faculty", "院系不合法");
            } else {
                patch.put("faculty", faculty);
                if (!Objects.equals(faculty, current.getFaculty()) && !input.hasField("major")) {
                    patch.put("major", null);
                }
            }
        }

        if (input.hasField("major")) {
            String major = input.getMajor();
            if (major == null || major.isEmpty()) {
                patch.put("major", null);
            } else if (!ProfileMajorCatalog.isValidForFaculty(faculty, major)) {
                errors.put("major", "专业必须属于所选院系");
            } else {
                patch.put("major", major);
            }
        }

        validateAndApplyLocation("location", input.hasField("location"), input.getLocation(), patch, errors);
        validateAndApplyLocation("hometown", input.hasField("hometown"), input.getHometown(), patch, errors);

        if (!errors.isEmpty()) {
            throw new InvalidPatch(errors);
        }

        boolean introductionPresent = patch.containsKey("introduction");
        String introduction = (String) patch.remove("introduction");
        if (!patch.isEmpty()) {
            profiles.updateProfilePatch(user.getUsername(), patch);
        }
        if (introductionPresent) {
            profiles.saveIntroduction(user.getUsername(), introduction);
        }
    }

    private void validateAndApplyLocation(String field, boolean present, LocationInput codes,
                                          Map<String, Object> patch, Map<String, String> errors) {
        if (!present) {
            return;
        }
        if (codes == null) {
            patch.put(field + "Region", null);
            patch.put(field + "State", null);
            patch.put(field + "City", null);
            return;
        }
        ProfileLocationValidator.ValidationResult result = locations.validate(codes.getRegion(), codes.getState(), codes.getCity());
        if (!result.isValid()) {
            errors.put(field, result.getErrorMessage());
        } else {
            patch.put(field + "Region", result.getRegion());
            patch.put(field + "State", result.getState());
            patch.put(field + "City", result.getCity());
        }
    }
}
