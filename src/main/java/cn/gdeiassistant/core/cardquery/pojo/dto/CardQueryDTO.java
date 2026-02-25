package cn.gdeiassistant.core.cardquery.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 饭卡查询条件入参 DTO。
 */
public class CardQueryDTO implements Serializable {

    @NotNull(message = "查询年份不能为空")
    @Min(value = 2008, message = "年份格式不正确")
    @Max(value = 2050, message = "年份格式不正确")
    private int year;

    @NotNull(message = "查询月份不能为空")
    @Min(value = 1, message = "月份格式不正确")
    @Max(value = 12, message = "月份格式不正确")
    private int month;

    @NotNull(message = "查询日期不能为空")
    @Min(value = 1, message = "日期格式不正确")
    @Max(value = 31, message = "日期格式不正确")
    private int date;

    public int getDate() { return date; }
    public void setDate(int date) { this.date = date; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}
