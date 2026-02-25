package cn.gdeiassistant.core.cetquery.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * 四六级准考证号视图（GET /api/cet/number 返回）。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CetNumberVO implements Serializable {

    private Long number;
    private String name;

    public CetNumberVO() {}

    public CetNumberVO(Long number, String name) {
        this.number = number;
        this.name = name;
    }

    public Long getNumber() { return number; }
    public void setNumber(Long number) { this.number = number; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
