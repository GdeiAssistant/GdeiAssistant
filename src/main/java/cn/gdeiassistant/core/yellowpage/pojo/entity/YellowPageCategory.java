package cn.gdeiassistant.core.yellowpage.pojo.entity;

import java.io.Serializable;

/**
 * Persistence projection for the {@code yellow_page_type} table.
 */
public class YellowPageCategory implements Serializable {

    private Integer typeCode;

    private String typeName;

    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
