package com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistantLogs.Charge;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.ChargeLog;

public interface ChargeMapper {

    public void insertChargeLog(ChargeLog chargeLog) throws Exception;
}
