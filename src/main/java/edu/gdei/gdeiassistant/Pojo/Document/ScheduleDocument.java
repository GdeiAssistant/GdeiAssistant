package edu.gdei.gdeiassistant.Pojo.Document;

import edu.gdei.gdeiassistant.Pojo.Entity.Schedule;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document
public class ScheduleDocument {

    /**
     * 主键ID
     */
    @Id
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 课表信息
     */
    private List<Schedule> scheduleList;

    /**
     * 更新时间
     */
    private Date updateDateTime;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public Date getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(Date updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
