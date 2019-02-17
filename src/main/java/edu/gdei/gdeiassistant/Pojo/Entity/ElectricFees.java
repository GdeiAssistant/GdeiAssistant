package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.gdei.gdeiassistant.Annotation.ExcelField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElectricFees implements Serializable {

    //年份
    private Integer year;

    //大楼
    private String buildingNumber;

    //房号
    private Integer roomNumber;

    //入住人数
    private Integer peopleNumber;

    //学院
    private String department;

    //学号
    private Long number;

    //姓名
    private String name;

    //用电数额
    private Float usedElectricAmount;

    //免费电额
    private Float freeElectricAmount;

    //计费电数
    private Float feeBasedElectricAmount;

    //电价
    private Float electricPrice;

    //总电费
    private Float totalElectricBill;

    //平均电费
    private Float averageElectricBill;

    @ExcelField(title = "大楼", align = 2, sort = 1)
    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    @ExcelField(title = "房号", align = 2, sort = 2)
    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    @ExcelField(title = "入住人数", align = 2, sort = 3)
    public Integer getPeopleNumber() {
        return peopleNumber;
    }

    public void setPeopleNumber(Integer peopleNumber) {
        this.peopleNumber = peopleNumber;
    }

    @ExcelField(title = "学院", align = 2, sort = 4)
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @ExcelField(title = "学号", align = 2, sort = 5)
    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    @ExcelField(title = "姓名", align = 2, sort = 6)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelField(title = "用电数额", align = 2, sort = 7)
    public Float getUsedElectricAmount() {
        return usedElectricAmount;
    }

    public void setUsedElectricAmount(Float usedElectricAmount) {
        this.usedElectricAmount = usedElectricAmount;
    }

    @ExcelField(title = "免费电额", align = 2, sort = 8)
    public Float getFreeElectricAmount() {
        return freeElectricAmount;
    }

    public void setFreeElectricAmount(Float freeElectricAmount) {
        this.freeElectricAmount = freeElectricAmount;
    }

    @ExcelField(title = "计费电数", align = 2, sort = 9)
    public Float getFeeBasedElectricAmount() {
        return feeBasedElectricAmount;
    }

    public void setFeeBasedElectricAmount(Float feeBasedElectricAmount) {
        this.feeBasedElectricAmount = feeBasedElectricAmount;
    }

    @ExcelField(title = "电价", align = 2, sort = 10)
    public Float getElectricPrice() {
        return electricPrice;
    }

    public void setElectricPrice(Float electricPrice) {
        this.electricPrice = electricPrice;
    }

    @ExcelField(title = "总电费", align = 2, sort = 11)
    public Float getTotalElectricBill() {
        return totalElectricBill;
    }

    public void setTotalElectricBill(Float totalElectricBill) {
        this.totalElectricBill = totalElectricBill;
    }

    @ExcelField(title = "平均电费", align = 2, sort = 12)
    public Float getAverageElectricBill() {
        return averageElectricBill;
    }

    public void setAverageElectricBill(Float averageElectricBill) {
        this.averageElectricBill = averageElectricBill;
    }

    @ExcelField(title = "年份", align = 2, sort = 13)
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
