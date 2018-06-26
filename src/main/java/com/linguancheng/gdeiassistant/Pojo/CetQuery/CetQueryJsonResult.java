package com.gdeiassistant.gdeiassistant.Pojo.CetQuery;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.Cet;

public class CetQueryJsonResult {

    private boolean success;

    private String errorMessage;

    private Cet cet;

    public Cet getCet() {
        return cet;
    }

    public void setCet(Cet cet) {
        this.cet = cet;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
