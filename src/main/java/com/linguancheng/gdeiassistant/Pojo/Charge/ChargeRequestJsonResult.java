package com.linguancheng.gdeiassistant.Pojo.Charge;

import com.linguancheng.gdeiassistant.Pojo.Entity.Charge;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ChargeRequestJsonResult {

    private String serverKeycode;

    private boolean success;

    private String errorMessage;

    private Charge charge;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public String getServerKeycode() {
        return serverKeycode;
    }

    public void setServerKeycode(String serverKeycode) {
        this.serverKeycode = serverKeycode;
    }
}
