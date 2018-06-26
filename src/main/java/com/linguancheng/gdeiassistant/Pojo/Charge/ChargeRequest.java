package com.linguancheng.gdeiassistant.Pojo.Charge;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ChargeRequest {

    @NotNull
    @Min(1)
    @Max(500)
    private Integer amount;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
