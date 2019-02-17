package edu.gdei.gdeiassistant.Pojo.Document;

import edu.gdei.gdeiassistant.Pojo.Entity.Schedule;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
public class CustomScheduleDocument {

    /**
     * 主键ID
     */
    @Id
    private String id;

    /**
     * 用户名
     */
    private String username;

    private Map<String, Schedule> scheduleMap;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Schedule> getScheduleMap() {
        return scheduleMap;
    }

    public void setScheduleMap(Map<String, Schedule> scheduleMap) {
        this.scheduleMap = scheduleMap;
    }
}
