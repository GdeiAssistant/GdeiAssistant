package cn.gdeiassistant.common.pojo.Document;

import cn.gdeiassistant.common.pojo.Entity.SpareRoom;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB 集合 "spare"：测试账号空课室查询模拟数据。
 * 文档字段：username, spareList
 */
@Document(collection = "spare")
public class SpareTestDocument {

    @Id
    private String id;

    private String username;

    /** 空课室列表（对应 POST /api/spare/query） */
    private List<SpareRoom> spareList;

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

    public List<SpareRoom> getSpareList() {
        return spareList;
    }

    public void setSpareList(List<SpareRoom> spareList) {
        this.spareList = spareList;
    }
}
