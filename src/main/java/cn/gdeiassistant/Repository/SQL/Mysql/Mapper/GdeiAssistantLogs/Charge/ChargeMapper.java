package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.Charge;

import cn.gdeiassistant.Pojo.Entity.ChargeLog;
import org.apache.ibatis.annotations.Insert;

public interface ChargeMapper {

    @Insert("insert into charge_log (username,amount,time) values(#{username},#{amount},now())")
    void insertChargeLog(ChargeLog chargeLog);
}