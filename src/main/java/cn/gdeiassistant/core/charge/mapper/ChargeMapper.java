package cn.gdeiassistant.core.charge.mapper;

import cn.gdeiassistant.core.charge.pojo.entity.ChargeLogEntity;
import org.apache.ibatis.annotations.Insert;

public interface ChargeMapper {

    @Insert("insert into charge_log (username,amount,time) values(#{username},#{amount},now())")
    void insertChargeLog(ChargeLogEntity chargeLog);
}