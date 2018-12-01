package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomSchedule implements Serializable {

    //课程长度
    @NotNull
    @Range(min = 1, max = 5)
    private Integer scheduleLength;

    //课程名称
    @NotBlank
    @Length(min = 1, max = 50)
    private String scheduleName;

    //上课地点
    @NotBlank
    @Length(min = 1, max = 25)
    private String scheduleLocation;

    //单元格位置
    @NotNull
    @Range(min = 0, max = 69)
    private Integer position;

    //最小的课程周数
    @NotNull
    @Range(min = 1, max = 20)
    private Integer minScheduleWeek;

    //最大的课程周数
    @NotNull
    @Range(min = 1, max = 20)
    private Integer maxScheduleWeek;

    public Integer getScheduleLength() {
        return scheduleLength;
    }

    public void setScheduleLength(Integer scheduleLength) {
        this.scheduleLength = scheduleLength;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getScheduleLocation() {
        return scheduleLocation;
    }

    public void setScheduleLocation(String scheduleLocation) {
        this.scheduleLocation = scheduleLocation;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getMinScheduleWeek() {
        return minScheduleWeek;
    }

    public void setMinScheduleWeek(Integer minScheduleWeek) {
        this.minScheduleWeek = minScheduleWeek;
    }

    public Integer getMaxScheduleWeek() {
        return maxScheduleWeek;
    }

    public void setMaxScheduleWeek(Integer maxScheduleWeek) {
        this.maxScheduleWeek = maxScheduleWeek;
    }
}
