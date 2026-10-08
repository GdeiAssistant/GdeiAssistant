package cn.gdeiassistant.core.profile.service;

import cn.gdeiassistant.common.exception.tokenvalidexception.TokenExpiredException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.userprofile.controller.support.ProfileLocationValidator;
import cn.gdeiassistant.core.userprofile.service.ProfileMajorCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ProfilePatchService {
    private final ProfileMapper profiles;
    private final UserCertificateService certificates;
    private final ProfileLocationValidator locations;
    public ProfilePatchService(ProfileMapper profiles, UserCertificateService certificates, ProfileLocationValidator locations) {
        this.profiles = profiles; this.certificates = certificates; this.locations = locations;
    }
    public static class InvalidPatch extends IllegalArgumentException {
        private final Map<String, String> errors;
        public InvalidPatch(Map<String, String> errors) { super("请检查个人资料"); this.errors = errors; }
        public Map<String, String> getErrors() { return errors; }
    }
    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    public void save(String sessionId, Map<String, Object> input) throws Exception {
        User user = certificates.getUserLoginCertificate(sessionId);
        if (user == null) throw new TokenExpiredException("登录凭证已过期，请重新登录");
        ProfileEntity current = profiles.lockUserProfile(user.getUsername());
        if (current == null) throw new cn.gdeiassistant.common.exception.databaseexception.UserNotExistException("查询的用户不存在");
        Map<String, String> errors = new LinkedHashMap<>();
        Map<String, Object> patch = new LinkedHashMap<>();
        Set<String> allowed = Set.of("nickname", "introduction", "birthday", "faculty", "major", "enrollment", "location", "hometown");
        if (input == null) throw new InvalidPatch(Map.of("profile", "请求参数不合法"));
        for (String key : input.keySet()) if (!allowed.contains(key)) errors.put(key, "不支持修改此字段");
        for (String key : List.of("nickname", "introduction")) if (input.containsKey(key)) {
            Object raw = input.get(key);
            String value = raw instanceof String ? ((String) raw).trim() : null;
            if (key.equals("introduction") && raw == null) value = "";
            if (value == null || (key.equals("nickname") && value.isEmpty()) || value.length() > (key.equals("nickname") ? 32 : 80)) errors.put(key, "长度不合法");
            else {
                String escaped = HtmlUtils.htmlEscape(value);
                if (escaped.length() > (key.equals("nickname") ? 32 : 80)) errors.put(key, "保存后的内容超过长度限制");
                else patch.put(key, escaped);
            }
        }
        if (input.containsKey("birthday")) {
            Object raw = input.get("birthday");
            if (raw == null) patch.put("birthday", null);
            else try {
                Map<?, ?> date = (Map<?, ?>) raw;
                LocalDate birthday = LocalDate.of(integer(date.get("year")), integer(date.get("month")), integer(date.get("date")));
                if (birthday.isAfter(LocalDate.now(ZoneId.of("Asia/Shanghai"))) || birthday.getYear() < 1900) throw new IllegalArgumentException();
                patch.put("birthday", java.sql.Date.valueOf(birthday));
            } catch (RuntimeException e) { errors.put("birthday", "生日不合法"); }
        }
        if (input.containsKey("enrollment")) {
            Object raw = input.get("enrollment");
            try {
                Integer year = raw == null ? null : integer(raw);
                if (year != null && (year < 1900 || year > LocalDate.now(ZoneId.of("Asia/Shanghai")).getYear())) throw new IllegalArgumentException();
                patch.put("enrollment", year);
            } catch (RuntimeException e) { errors.put("enrollment", "入学年份不合法"); }
        }
        Integer faculty = current.getFaculty();
        if (input.containsKey("faculty")) try {
            faculty = integer(input.get("faculty"));
            if (!UserProfileService.getFacultyMap().containsKey(faculty)) throw new IllegalArgumentException();
            patch.put("faculty", faculty);
            if (!Objects.equals(faculty, current.getFaculty()) && !input.containsKey("major")) patch.put("major", null);
        } catch (RuntimeException e) { errors.put("faculty", "院系不合法"); }
        if (input.containsKey("major")) {
            Object raw = input.get("major");
            if (raw == null || "".equals(raw)) patch.put("major", null);
            else if (!(raw instanceof String) || !ProfileMajorCatalog.isValidForFaculty(faculty, (String) raw)) errors.put("major", "专业必须属于所选院系");
            else patch.put("major", raw);
        }
        for (String field : List.of("location", "hometown")) if (input.containsKey(field)) {
            Object raw = input.get(field);
            if (raw == null) {
                patch.put(field + "Region", null); patch.put(field + "State", null); patch.put(field + "City", null);
            } else try {
                Map<?, ?> codes = (Map<?, ?>) raw;
                ProfileLocationValidator.ValidationResult result = locations.validate(string(codes.get("region")), string(codes.get("state")), string(codes.get("city")));
                if (!result.isValid()) errors.put(field, result.getErrorMessage());
                else { patch.put(field + "Region", result.getRegion()); patch.put(field + "State", result.getState()); patch.put(field + "City", result.getCity()); }
            } catch (RuntimeException e) { errors.put(field, "地区代码不合法"); }
        }
        if (!errors.isEmpty()) throw new InvalidPatch(errors);
        boolean introductionPresent = patch.containsKey("introduction");
        String introduction = (String) patch.remove("introduction");
        if (!patch.isEmpty()) profiles.updateProfilePatch(user.getUsername(), patch);
        if (introductionPresent) profiles.saveIntroduction(user.getUsername(), introduction);
    }
    private static int integer(Object value) {
        if (!(value instanceof Number) || value instanceof Float || value instanceof Double) throw new IllegalArgumentException();
        return Math.toIntExact(((Number) value).longValue());
    }
    private static String string(Object value) {
        if (value == null) return null;
        if (!(value instanceof String)) throw new IllegalArgumentException();
        return (String) value;
    }
}
