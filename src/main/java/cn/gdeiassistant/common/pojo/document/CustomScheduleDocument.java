package cn.gdeiassistant.common.pojo.document;

import cn.gdeiassistant.common.pojo.entity.Schedule;
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

    private Long version;

    public Long getVersion() { return version; }

    public void setVersion(Long version) { this.version = version; }

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
