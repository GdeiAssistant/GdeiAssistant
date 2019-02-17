package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantLogs.Charge;

import edu.gdei.gdeiassistant.Pojo.Entity.ChargeLog;

public interface ChargeMapper {

    public void insertChargeLog(ChargeLog chargeLog) throws Exception;
}
