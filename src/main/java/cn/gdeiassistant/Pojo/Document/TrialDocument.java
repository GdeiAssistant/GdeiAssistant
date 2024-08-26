package cn.gdeiassistant.Pojo.Document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
public class TrialDocument<T> {

    /**
     * 主键ID
     */
    @Id
    private String id;

    /**
     * 数据类型
     */
    private String type;

    /**
     * 数据信息
     */
    private T data;

    /**
     * 更新时间
     */
    private Date updateDateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Date getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(Date updateDateTime) {
        this.updateDateTime = updateDateTime;
    }
}
