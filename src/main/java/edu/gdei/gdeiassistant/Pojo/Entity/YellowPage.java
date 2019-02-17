package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.gdei.gdeiassistant.Annotation.ExcelField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YellowPage implements Serializable {

    private Integer id;

    private Integer typeCode;

    private String typeName;

    private String section;

    private String campus;

    private String majorPhone;

    private String minorPhone;

    private String address;

    private String email;

    private String website;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ExcelField(title = "类别", align = 2, sort = 1)
    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    @ExcelField(title = "单位", align = 2, sort = 2)
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @ExcelField(title = "区域", align = 2, sort = 3)
    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    @ExcelField(title = "主要电话", align = 2, sort = 4)
    public String getMajorPhone() {
        return majorPhone;
    }

    public void setMajorPhone(String majorPhone) {
        this.majorPhone = majorPhone;
    }

    @ExcelField(title = "次要电话", align = 2, sort = 5)
    public String getMinorPhone() {
        return minorPhone;
    }

    public void setMinorPhone(String minorPhone) {
        this.minorPhone = minorPhone;
    }

    @ExcelField(title = "地址", align = 2, sort = 6)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @ExcelField(title = "邮箱", align = 2, sort = 7)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ExcelField(title = "网站", align = 2, sort = 8)
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
