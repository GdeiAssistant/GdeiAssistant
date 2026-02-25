package cn.gdeiassistant.core.charge.pojo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 充值记录表 charge_log 的持久化映射，column 与库表一致：id, username, amount, time。
 */
public class ChargeLogEntity implements Serializable {

    private Integer id;
    private String username;
    private Integer amount;
    private Date time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
