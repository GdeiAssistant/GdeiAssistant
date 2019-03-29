package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantLogs.Charge;

import edu.gdei.gdeiassistant.Pojo.Entity.ChargeLog;
import org.apache.ibatis.annotations.Insert;

public interface ChargeMapper {

    @Insert("insert into charge_log (username,amount,time) values(#{username},#{amount},now())")
    public void insertChargeLog(ChargeLog chargeLog) throws Exception;
}