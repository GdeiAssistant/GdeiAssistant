package cn.gdeiassistant.core.userprofile.controller.request;

import cn.gdeiassistant.core.profile.service.ProfilePatchService;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.annotation.JsonDeserialize;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Partial profile update. Fields that are absent stay unchanged; explicit
 * null clears the field where the service allows it.
 */
public class ProfilePatchRequest implements ProfilePatchService.PatchInput {

    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String nickname;
    @Nullable
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String introduction;
    @Nullable
    @JsonDeserialize(using = StrictIntegerDeserializer.class)
    private Integer enrollment;
    @JsonDeserialize(using = StrictIntegerDeserializer.class)
    private Integer faculty;
    @Nullable
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private String major;
    @Nullable
    private BirthdayPatch birthday;
    @Nullable
    private LocationPatch location;
    @Nullable
    private LocationPatch hometown;

    @JsonIgnore
    private final Set<String> provided = new LinkedHashSet<>();

    @JsonIgnore
    private final Map<String, Object> unknown = new LinkedHashMap<>();

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        provided.add("nickname");
        this.nickname = nickname;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        provided.add("introduction");
        this.introduction = introduction;
    }

    public Integer getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Integer enrollment) {
        provided.add("enrollment");
        this.enrollment = enrollment;
    }

    public Integer getFaculty() {
        return faculty;
    }

    public void setFaculty(Integer faculty) {
        provided.add("faculty");
        this.faculty = faculty;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        provided.add("major");
        this.major = major;
    }

    public BirthdayPatch getBirthday() {
        return birthday;
    }

    public void setBirthday(BirthdayPatch birthday) {
        provided.add("birthday");
        this.birthday = birthday;
    }

    public LocationPatch getLocation() {
        return location;
    }

    public void setLocation(LocationPatch location) {
        provided.add("location");
        this.location = location;
    }

    public LocationPatch getHometown() {
        return hometown;
    }

    public void setHometown(LocationPatch hometown) {
        provided.add("hometown");
        this.hometown = hometown;
    }

    @JsonAnySetter
    public void putUnknown(String name, Object value) {
        unknown.put(name, value);
    }

    @Override
    public boolean hasField(String name) {
        return name != null && provided.contains(name);
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getUnknownFields() {
        return Collections.unmodifiableMap(unknown);
    }

    public static class BirthdayPatch implements ProfilePatchService.BirthdayInput {
        @JsonDeserialize(using = StrictIntegerDeserializer.class)
        private Integer year;
        @JsonDeserialize(using = StrictIntegerDeserializer.class)
        private Integer month;
        @JsonDeserialize(using = StrictIntegerDeserializer.class)
        private Integer date;

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }

        public Integer getDate() {
            return date;
        }

        public void setDate(Integer date) {
            this.date = date;
        }
    }

    public static class LocationPatch implements ProfilePatchService.LocationInput {
        @JsonDeserialize(using = StrictStringDeserializer.class)
        private String region;
        @Nullable
        @JsonDeserialize(using = StrictStringDeserializer.class)
        private String state;
        @Nullable
        @JsonDeserialize(using = StrictStringDeserializer.class)
        private String city;

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }
}
