package cn.gdeiassistant.core.cetquery.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * POST /api/cet/number 请求体：保存准考证号。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CetNumberSaveDTO implements Serializable {

    @Size(min = 15, max = 15, message = "准考证号必须为15位")
    private String number;

    @Size(max = 20)
    private String name;

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
