package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.Data;

import edu.gdei.gdeiassistant.Pojo.Entity.ChargeLog;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface LogDataMapper {

    @Select("select amount,time from charge_log where username=#{username}")
    @Results(id = "ChargeLog", value = {
            @Result(property = "amount", column = "amount"),
            @Result(property = "time", column = "time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    public List<ChargeLog> selectChargeLogList(String username);
}
