package com.linguancheng.gdeiassistant.Pojo.Charge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.linguancheng.gdeiassistant.Annotation.EncryptData;
import com.linguancheng.gdeiassistant.Pojo.Entity.Charge;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@EncryptData()
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChargeResult implements Serializable {

    private Charge charge;

    private String signature;

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public ChargeResult() {

    }

    public ChargeResult(Charge charge, String signature) {
        this.charge = charge;
        this.signature = signature;
    }
}
