package cn.gdeiassistant.common.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule implements Serializable, Entity {

    @JsonProperty("id")
    private String id;

    @JsonProperty("scheduleLength")
    private Integer scheduleLength;

    @JsonProperty("scheduleName")
    private String scheduleName;

    @JsonProperty("scheduleType")
    private String scheduleType;

    @JsonProperty("scheduleLesson")
    private String scheduleLesson;

    @JsonProperty("scheduleTeacher")
    private String scheduleTeacher;

    @JsonProperty("scheduleLocation")
    private String scheduleLocation;

    @JsonProperty("colorCode")
    private String colorCode;

    /**
     * 单元格位置Position、行Row、列Columm的关系
     * Position = Row * 7 + Column
     * Row = Position / 7
     * Column = Position % 7
     */

    @JsonProperty("position")
    private Integer position;

    @JsonProperty("row")
    private Integer row;

    @JsonProperty("column")
    private Integer column;

    @JsonProperty("minScheduleWeek")
    private Integer minScheduleWeek;

    @JsonProperty("maxScheduleWeek")
    private Integer maxScheduleWeek;

    @JsonProperty("scheduleWeek")
    private String scheduleWeek;

    /**
     * 是否为自定义课程（来自 CustomScheduleDocument）。仅用于合并结果打标，不参与 MongoDB 持久化。
     * 教务/缓存课程不设置，保持 null；合并时对自定义课显式设为 true。
     */
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isCustom;

    public Boolean getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(Boolean isCustom) {
        this.isCustom = isCustom;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

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

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getScheduleLesson() {
        return scheduleLesson;
    }

    public void setScheduleLesson(String scheduleLesson) {
        this.scheduleLesson = scheduleLesson;
    }

    public String getScheduleTeacher() {
        return scheduleTeacher;
    }

    public void setScheduleTeacher(String scheduleTeacher) {
        this.scheduleTeacher = scheduleTeacher;
    }

    public String getScheduleLocation() {
        return scheduleLocation;
    }

    public void setScheduleLocation(String scheduleLocation) {
        this.scheduleLocation = scheduleLocation;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMaxScheduleWeek() {
        return maxScheduleWeek;
    }

    public void setMaxScheduleWeek(Integer maxScheduleWeek) {
        this.maxScheduleWeek = maxScheduleWeek;
    }

    public Integer getMinScheduleWeek() {
        return minScheduleWeek;
    }

    public void setMinScheduleWeek(Integer minScheduleWeek) {
        this.minScheduleWeek = minScheduleWeek;
    }

    public String getScheduleWeek() {
        return scheduleWeek;
    }

    public void setScheduleWeek(String scheduleWeek) {
        this.scheduleWeek = scheduleWeek;
    }
}
