package com.linguancheng.gdeiassistant.Pojo.CetQuery;

import com.linguancheng.gdeiassistant.Pojo.Entity.Cet;

public class CetQueryJsonResult {

    private boolean success;

    private String message;

    private Cet cet;

    public Cet getCet() {
        return cet;
    }

    public void setCet(Cet cet) {
        this.cet = cet;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
